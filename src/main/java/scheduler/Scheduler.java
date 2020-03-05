package scheduler;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map.Entry;

import elevator.Elevator;
import elevator.ElevatorAction;
import elevator.ElevatorData;
import elevator.ElevatorEvent;
import elevator.ElevatorResponse;
import elevator.ElevatorStatus;
import floor.FloorData;
import floor.FloorSubsystem;

/**
 * Coordinates the elevator and floor subsystems.
 */
public class Scheduler implements Runnable {
    public static enum SchedulerState {
        WAITING,
        SCHEDULING_ELEVATOR,
        WAITING_FOR_ELEVATOR_RESPONSE,
        HANDLING_ELEVATOR_RESPONSE,
    }

    private final HashMap<Integer, ArrayDeque<ElevatorEvent>> elevatorEvents;
    
    private final HashMap<Integer, Integer> elevatorLocations;
    
    private final HashMap<Integer, ElevatorStatus> elevatorStatuses;

    private SchedulerState state;

    public Scheduler() {
        this.elevatorEvents = new HashMap<>();
        this.elevatorLocations = new HashMap<>();
        this.state = SchedulerState.WAITING;
    }

    @Override
    public void run() {
    }

    /**
     * Registers an {@link Elevator} with the {@link Scheduler}, so that the
     * {@link Scheduler} can use it.
     *
     * @param id the {@link Elevator}'s id
     */
    public void registerElevator(final int id) {
        this.elevatorEvents.put(id, new ArrayDeque<>());
        this.elevatorLocations.put(id, 0); // All elevators start at the ground floor.
    }

    public synchronized void addElevatorEvent(final ElevatorEvent event) {
        this.elevatorEvents.get(event.getData().getElevatorId()).add(event);
        this.notifyAll();
    }

    /**
     * Adds an {@link ElevatorEvent} to the {@link Scheduler}'s queue.
     *
     * @param floorData the floor information from where the request came
     */
    public synchronized void scheduleElevator(final FloorData floorData) {
        while (this.state != SchedulerState.WAITING) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                System.err.println(e);
            }
        }

        this.state = SchedulerState.SCHEDULING_ELEVATOR;

        // Get the elevator for the job.
        final ClosestElevator closestElevator = this.getClosestElevatorToFloor(floorData.getFloor());
        final int elevatorId = closestElevator.elevatorId;
        int numFloorsToTravel = closestElevator.numFloors;
        int currentLocation = this.elevatorLocations.get(elevatorId);

        // Move the elevator to the floor if necessary.
        if (numFloorsToTravel > 0) {
            this.createEventsToMoveElevatorToFloor(elevatorId,
                                                   currentLocation,
                                                   floorData.getFloor(),
                                                   numFloorsToTravel);
        }

        // Determine the elevator's location after it moves to the floor.
        if (currentLocation > floorData.getFloor()) {
            currentLocation -= numFloorsToTravel;
        } else {
            currentLocation += numFloorsToTravel;
        }

        numFloorsToTravel = Math.abs(floorData.getDestination() - currentLocation);

        // Move the elevator to the destination if we're not already there.
        if (numFloorsToTravel > 0) {
            this.createEventsToMoveElevatorToFloor(elevatorId,
                                                   currentLocation,
                                                   floorData.getDestination(),
                                                   numFloorsToTravel);
        }

        this.elevatorEvents.get(elevatorId).add(new ElevatorEvent(new ElevatorData(elevatorId,
                                                                                   currentLocation,
                                                                                   null),
                                                                  ElevatorAction.DESTINATION_REACHED));

        this.notifyAll();
    }

    /**
     * Adds the events to the queue that will move the {@link Elevator} to the desired floor.
     *
     * @param elevatorId        the ID of the {@link Elevator}
     * @param currentFloor      the current floor of the {@link Elevator}
     * @param floor             the floor to go to
     * @param numFloorsToTravel the number of floors to travel
     */
    private void createEventsToMoveElevatorToFloor(final int elevatorId,
                                                   final int currentFloor,
                                                   final int floor,
                                                   final int numFloorsToTravel) {
        final ElevatorData elevatorData = new ElevatorData(elevatorId, currentFloor, null);
        final ElevatorAction action =
                currentFloor > floor ? ElevatorAction.MOVE_DOWN : ElevatorAction.MOVE_UP;

        this.elevatorEvents.get(elevatorId).add(new ElevatorEvent(elevatorData, ElevatorAction.CLOSE_DOORS));

        // Add the events to move the elevator.
        for (int i = 0; i < numFloorsToTravel; i++) {
            this.elevatorEvents.get(elevatorId).add(new ElevatorEvent(elevatorData, action));
        }

        this.elevatorEvents.get(elevatorId).add(new ElevatorEvent(elevatorData, ElevatorAction.STOP_MOVING));
        this.elevatorEvents.get(elevatorId).add(new ElevatorEvent(elevatorData, ElevatorAction.OPEN_DOORS));
    }

    /**
     * Gets the first {@link ElevatorAction} from the queue.
     *
     * @return the first {@link ElevatorAction} in the queue
     */
    public synchronized ElevatorAction getElevatorAction(final int id) {
        while (this.elevatorEvents.get(id).isEmpty()) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                System.err.println(e);
            }
        }

        ElevatorAction action = this.elevatorEvents.get(id).getFirst().getAction();
        this.state = SchedulerState.WAITING_FOR_ELEVATOR_RESPONSE;

        this.notifyAll();
        return action;
    }

    /**
     * Handles an {@link ElevatorResponse}.
     *
     * @param elevatorId the ID of the {@link Elevator}
     * @param response   success or failure
     */
    public synchronized void handleElevatorResponse(final int elevatorId, final ElevatorResponse response) {
        this.state = SchedulerState.HANDLING_ELEVATOR_RESPONSE;

        switch (response) {
        case DESTINATION_REACHED:
            this.state = SchedulerState.WAITING;
            break;
        // There are still more floors to travel until destination.
        case SUCCESS:
            this.state = SchedulerState.WAITING_FOR_ELEVATOR_RESPONSE;
            break;
        case FAILURE:
            return;
        }

        this.elevatorEvents.get(elevatorId).removeFirst();
        this.notifyAll();
    }

    /**
     * Updates an {@link Elevator}'s location.
     *
     * @param elevatorId the ID of the {@link Elevator} to update
     * @param floor      the floor the {@link Elevator} is on
     */
    public synchronized void updateElevatorLocation(final int elevatorId, final int floor) {
        this.elevatorLocations.put(elevatorId, floor);
        this.notifyAll();
    }

    public SchedulerState getState() {
        return this.state;
    }

    /**
     * Returns the closest elevator to the specified floor.
     *
     * @return the ID and number of floors to travel for the {@link Elevator}
     *         closest to the specified floor.
     */
    private ClosestElevator getClosestElevatorToFloor(final int floor) {
        int closestElevator = 1;
        int closestDistance = FloorSubsystem.MAX_FLOORS;
        for (final Entry<Integer, Integer> entry : this.elevatorLocations.entrySet()) {
            final int distance = Math.abs(entry.getValue() - floor);

            if (distance < closestDistance) {
                closestDistance = distance;
                closestElevator = entry.getKey();
            }
        }

        return new ClosestElevator(closestElevator, closestDistance);
    }
    
    
    private int getSexiestElevator(final int floor, Direction direction) {
        int finalElevatorId;
        
        int tempElevatorId;
        ElevatorStatus tempElevatorStatus;
        Boolean anotherElevatorOnRoute = false;
        
        for (final Entry<Integer, ElevatorStatus> entry : this.elevatorStatuses.entrySet()) {
            
            tempElevatorId = entry.getKey();
            tempElevatorStatus = entry.getValue();
            
            if(tempElevatorStatus.getDirection() == NONE) {
                finalElevatorId = tempElevatorId;
                break;
            }
            
            if(tempElevatorStatus.getDirection() == direction && direction == UP) {
                if(tempElevatorStatus.getCurrentFloor() < floor) {
                    if(tempElevatorStatus.getStopsBefore(floor) < this.elevatorStatuses.get(elevatorId).getStopsBefore(floor)) {
                        int elevatorId = tempElevatorId;
                        anotherElevatorOnRoute = true;
                    }
                }
            }
            
            else if(tempElevatorStatus.getDirection() == direction && direction == DOWN) {
                if(tempElevatorStatus.getCurrentFloor() > floor) {
                    if(tempElevatorStatus.getStopsBefore(floor) < this.elevatorStatuses.get(elevatorId).getStopsBefore(floor)) {
                        int elevatorId = tempElevatorId;
                        anotherElevatorOnRoute = true;
                    }
                }     
            }
            
           
            if(tempElevatorStatus.getStopsBefore(floor) < this.elevatorStatuses.get(elevatorId).getStopsBefore() && !anotherElevatorOnRoute) {
                int elevatorId = tempElevatorId;
            }
        
        }

        this.elevatorStatuses.get(finalElevatorId).addStop(floor);
        
        return finalElevatorId;
    }
    
    
    
    
    
}

/**
 * Represents the elevator that is closest to a specific floor, and how many
 * floors it must travel to reach it.
 */
final class ClosestElevator {
    public final int elevatorId;
    public final int numFloors;

    public ClosestElevator(final int elevatorId, final int numFloors) {
        this.elevatorId = elevatorId;
        this.numFloors = numFloors;
    }
}
