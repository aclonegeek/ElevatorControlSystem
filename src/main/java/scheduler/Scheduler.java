package scheduler;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map.Entry;

import elevator.Elevator;
import elevator.ElevatorAction;
import elevator.ElevatorData;
import elevator.ElevatorEvent;
import elevator.ElevatorResponse;
import elevator.ElevatorState;
import floor.FloorData;
import floor.FloorData.ButtonState;
import floor.FloorSubsystem;

/**
 * Coordinates the elevator and floor subsystems.
 */
public class Scheduler implements Runnable {
    public static enum SchedulerState {
        WAITING, SCHEDULING_ELEVATOR, WAITING_FOR_ELEVATOR_RESPONSE, HANDLING_ELEVATOR_RESPONSE,
    }

    private final HashMap<Integer, ArrayDeque<ElevatorEvent>> elevatorEvents;

    private final HashMap<Integer, Integer> elevatorLocations;

    private final HashMap<Integer, ElevatorStatus> elevatorStatuses;

    private SchedulerState state;

    public Scheduler() {
        this.elevatorStatuses = new HashMap<>();
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

        // Get the best elevator for the job.
        final BestElevator bestElevator =
                this.getBestElevator(floorData.getFloor(), checkDirection(floorData.getButtonState()));
        final int elevatorId = bestElevator.elevatorId;
        int numFloorsToTravel = bestElevator.numFloors;
        int currentLocation = this.elevatorLocations.get(elevatorId);

        // Move the elevator to the floor if necessary.
        if (numFloorsToTravel > 0) {
            this.createEventsToMoveElevatorToFloor(elevatorId, currentLocation, floorData.getFloor(),
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
            this.createEventsToMoveElevatorToFloor(elevatorId, currentLocation, floorData.getDestination(),
                    numFloorsToTravel);
        }

        this.elevatorEvents.get(elevatorId).add(new ElevatorEvent(
                new ElevatorData(elevatorId, currentLocation, null), ElevatorAction.DESTINATION_REACHED));

        this.notifyAll();
    }

    /**
     * Adds the events to the queue that will move the {@link Elevator} to the
     * desired floor.
     *
     * @param elevatorId        the ID of the {@link Elevator}
     * @param currentFloor      the current floor of the {@link Elevator}
     * @param floor             the floor to go to
     * @param numFloorsToTravel the number of floors to travel
     */
    private void createEventsToMoveElevatorToFloor(final int elevatorId, final int currentFloor,
            final int floor, final int numFloorsToTravel) {
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

    public HashMap<Integer, ElevatorStatus> getElevatorStatuses() {
        return this.elevatorStatuses;
    }

    public void addElevatorStatus(final int elevatorID, final ElevatorStatus elevatorStatus) {
        this.elevatorStatuses.put(elevatorID, elevatorStatus);
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
    public ElevatorState checkDirection(ButtonState state) {
        if (state == ButtonState.UP) {
            return ElevatorState.MOVING_UP;
        } else if (state == ButtonState.DOWN) {
            return ElevatorState.MOVING_DOWN;
        } else {
            return ElevatorState.IDLE_DOOR_CLOSED;
        }
    }

    public BestElevator getBestElevator(final int floor, final ElevatorState state) {
        int bestElevatorID = -1;
        int tempElevatorID;
        ElevatorStatus tempElevatorStatus;
        Boolean anotherElevatorOnRoute = false;

        for (final Entry<Integer, ElevatorStatus> entry : this.elevatorStatuses.entrySet()) {
            tempElevatorID = entry.getKey();
            tempElevatorStatus = entry.getValue();

            // Init the bestId to the first ID.
            if (bestElevatorID == -1) {
                bestElevatorID = tempElevatorID;
            }

            int tempStopsBetween = getStopsBetween(tempElevatorStatus, floor);
            int bestStopsBetween = getStopsBetween(this.elevatorStatuses.get(bestElevatorID), floor);

            // Best elevator is idle.
            if (tempElevatorStatus.getState() == ElevatorState.IDLE_DOOR_CLOSED ||
                    tempElevatorStatus.getState() == ElevatorState.IDLE_DOOR_OPEN) {
                bestElevatorID = tempElevatorID;
                break;
            }

            // Best elevator is one moving with the floor on its path going upwards.
            if (tempElevatorStatus.getState() == state && state == ElevatorState.MOVING_UP) {
                if (tempElevatorStatus.getCurrentFloor() < floor) {
                    if (tempStopsBetween <= bestStopsBetween) {
                        bestElevatorID = tempElevatorID;
                        anotherElevatorOnRoute = true;
                    }
                }
            }

            // Best elevator is one moving with the floor on its path going downwards.
            else if (tempElevatorStatus.getState() == state && state == ElevatorState.MOVING_DOWN) {
                if (tempElevatorStatus.getCurrentFloor() > floor) {
                    if (tempStopsBetween <= bestStopsBetween) {
                        bestElevatorID = tempElevatorID;
                        anotherElevatorOnRoute = true;
                    }
                }
            }

            // Best elevator is one the one with least stops between.
            if (tempElevatorStatus.getDestinations().size() <= this.elevatorStatuses.get(bestElevatorID)
                    .getDestinations().size() && !anotherElevatorOnRoute) {
                bestElevatorID = tempElevatorID;
            }
        }

        // Add destination to best elevators destinations.
        System.out.println(floor);
        this.elevatorStatuses.get(bestElevatorID).addDestination(floor);

        int distance = Math.abs(this.elevatorStatuses.get(bestElevatorID).getCurrentFloor() - floor);

        return new BestElevator(bestElevatorID, distance);
    }

    private int getStopsBetween(final ElevatorStatus elevatorStatus, final int floor) {
        int floorsBetween = 0;

        for (int destination : elevatorStatus.getDestinations()) {
            if (destination < floor && destination > elevatorStatus.getCurrentFloor() ||
                    destination > floor && destination < elevatorStatus.getCurrentFloor()) {
                floorsBetween++;
            }
        }
        return floorsBetween;
    }
}

/**
 * Represents the elevator that is closest to a specific floor, and how many
 * floors it must travel to reach it.
 */
final class BestElevator {
    public final int elevatorId;
    public final int numFloors;

    public BestElevator(final int elevatorId, final int numFloors) {
        this.elevatorId = elevatorId;
        this.numFloors = numFloors;
    }
}
