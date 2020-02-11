package scheduler;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map.Entry;

import elevator.ElevatorAction;
import elevator.ElevatorData;
import elevator.ElevatorEvent;
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
    public void run() {}

    /**
     * Registers an {@link Elevator} with the {@link Scheduler}, so that the {@link Scheduler} can use it.
     *
     * @param id the {@link Elevator}'s id
     */
    public void registerElevator(final int id) {
        this.elevatorEvents.put(id, new ArrayDeque<>());
        this.elevatorLocations.put(id, 0); // All elevators start at the ground floor.
    }

    /**
     * Adds an {@link ElevatorEvent} to the {@link Scheduler}'s queue.
     *
     * @param floorData the floor information from where the request came
     */
    public synchronized void scheduleElevator(final FloorData floorData) {
        final int elevatorId = this.getClosestElevatorToFloor(floorData.getFloor());
        final ElevatorData elevatorData = new ElevatorData(elevatorId,
                                                           this.elevatorLocations.get(elevatorId),
                                                           floorData.getFloor(),
                                                           null); // TODO: Time stuff.
        this.elevatorEvents.get(elevatorId).add(new ElevatorEvent(elevatorData,
                                                                  ElevatorAction.START_MOVING));

        this.notifyAll();
    }

    /**
     * Removes an {@link ElevatorAction} from the queue.
     *
     * @return the first {@link ElevatorAction} in the queue
     */
    public synchronized ElevatorAction removeElevatorAction(final int id) {
        while (this.elevatorEvents.get(id).isEmpty()) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                System.err.println(e);
            }
        }

        ElevatorAction action = this.elevatorEvents.get(id).removeFirst().getAction();

        this.notifyAll();
        return action;
    }

    /**
     * Returns the closest elevator to the specified floor.
     *
     * @return the ID of the {@link Elevator} closest to the specified floor.
     */
    private int getClosestElevatorToFloor(final int floor) {
        int closestElevator = 1;
        int closestDistance = FloorSubsystem.MAX_FLOORS;
        for (final Entry<Integer, Integer> entry : this.elevatorLocations.entrySet()) {
            final int distance = Math.abs(entry.getValue() - floor);

            if (distance < closestDistance) {
                closestDistance = distance;
                closestElevator = entry.getKey();
            }
        }

        return closestElevator;
    }
}
