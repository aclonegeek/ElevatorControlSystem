package scheduler;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map.Entry;

import elevator.ElevatorAction;
import elevator.ElevatorData;
import elevator.ElevatorEvent;
import elevator.ElevatorResponse;
import floor.FloorData;
import floor.FloorSubsystem;

/**
 * Coordinates the elevator and floor subsystems.
 */
public class Scheduler implements Runnable {
    private HashMap<Integer, ArrayDeque<ElevatorEvent>> elevatorEvents;
    private HashMap<Integer, Integer> elevatorLocations;

    public Scheduler() {
        this.elevatorEvents = new HashMap<>();
        this.elevatorLocations = new HashMap<>();
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
    public void scheduleElevator(final FloorData floorData) {
        final ClosestElevator closestElevator = this.getClosestElevatorToFloor(floorData.getFloor());
        final int elevatorId = closestElevator.elevatorId;
        final int numFloors = closestElevator.numFloors;

        final ElevatorData elevatorData =
                new ElevatorData(elevatorId, this.elevatorLocations.get(elevatorId), null);

        synchronized (this) {
            this.elevatorEvents.get(elevatorId)
                    .add(new ElevatorEvent(elevatorData, ElevatorAction.CLOSE_DOORS));

            for (int i = 0; i < numFloors; i++) {
                this.elevatorEvents.get(elevatorId)
                        .add(new ElevatorEvent(elevatorData,
                                floorData.getButtonState() == FloorData.ButtonState.UP
                                        ? ElevatorAction.MOVE_UP
                                        : ElevatorAction.MOVE_DOWN));
            }

            this.elevatorEvents.get(elevatorId)
                    .add(new ElevatorEvent(elevatorData, ElevatorAction.STOP_MOVING));
            this.elevatorEvents.get(elevatorId)
                    .add(new ElevatorEvent(elevatorData, ElevatorAction.OPEN_DOORS));
        }

        this.notifyAll();
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

        this.notifyAll();
        return action;
    }

    /**
     * Handles an {@link ElevatorResponse}.
     *
     * @param elevatorId the ID of the elevator
     * @param response   success or failure
     */
    public synchronized void handleElevatorResponse(final int elevatorId, final ElevatorResponse response) {
        if (response == ElevatorResponse.FAILURE) {
            // TODO: Handle the error - try resending?
            return;
        }

        this.elevatorEvents.get(elevatorId).removeFirst();
        this.notifyAll();
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
}

final class ClosestElevator {
    public final int elevatorId;
    public final int numFloors;

    public ClosestElevator(final int elevatorId, final int numFloors) {
        this.elevatorId = elevatorId;
        this.numFloors = numFloors;
    }
}
