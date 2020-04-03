package scheduler;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import elevator.ElevatorAction;
import elevator.ElevatorState;

public class ElevatorStatus {
    private final int id;
    private ElevatorState state;
    private final ArrayList<Integer> destinations;
    private int currentFloor;

    private Timer timer;
    private TimerTask movementTimerTask;
    private TimerTask doorFaultTimerTask;

    private Scheduler scheduler;

    public ElevatorStatus(final int id, final Scheduler scheduler, final ElevatorState state,
            final int currentFloor) {
        this.id = id;
        this.scheduler = scheduler;
        this.destinations = new ArrayList<Integer>();
        this.state = state;
        this.currentFloor = currentFloor;
        this.timer = new Timer();
    }

    public void startMovementTimerTask() {
        this.movementTimerTask = new TimerTask() {
            @Override
            public void run() {
                System.out.println("Fault detected for elevator " + id);
                scheduler.rerouteFaultedElevator(id, state);
                scheduler.sendElevatorAction(id, ElevatorAction.STOP_MOVING);
            }
        };
        // It takes 1 second for the elevator to move between floors.
        // If the {@link TimerTask} isn't cancelled after 2 seconds
        // (i.e. the arrival sensor hasn't notified us), then there's a fault.
        this.timer.schedule(movementTimerTask, 1500);
    }

    public void startDoorFaultTimerTask() {
        ElevatorStatus es = this;

        this.doorFaultTimerTask = new TimerTask() {
            @Override
            public void run() {
                // No door fault.
                if (state != ElevatorState.DOOR_CLOSED_FOR_MOVING) {
                    return;
                }

                System.out.println("Door fault detected for elevator " + id);
                scheduler.resendElevatorAction(id, es);
            }
        };
        // If the elevator state doesn't change from door closed within 100 ms,
        // there's a problem.
        this.timer.schedule(doorFaultTimerTask, 100);
    }

    public void stopMovementTimerTask() {
        this.movementTimerTask.cancel();
    }

    public void addDestination(final int floor) {
        this.destinations.add(floor);
    }

    public void addDestinations(final ArrayList<Integer> floors) {
        this.destinations.addAll(floors);
    }

    public ArrayList<Integer> getDestinations() {
        return this.destinations;
    }

    public void removeDestination() {
        this.destinations.remove(0);
    }

    public int getCurrentFloor() {
        return this.currentFloor;
    }

    public void setCurrentFloor(final int floor) {
        this.currentFloor = floor;
    }

    public ElevatorState getState() {
        return this.state;
    }

    public void setState(final ElevatorState state) {
        this.state = state;
    }
}
