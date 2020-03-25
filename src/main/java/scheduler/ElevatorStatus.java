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
    private TimerTask timerTask;

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

    public void startTimer() {
        this.timerTask = new TimerTask() {
            @Override
            public void run() {
                System.out.println("Fault detected for elevator " + id);
                scheduler.sendElevatorAction(id, ElevatorAction.STOP_MOVING);
                scheduler.getElevatorStatuses().remove(id);
            }
        };
        // It takes 1 second for the elevator to move between floors.
        // If the {@link TimerTask} isn't cancelled after 2 seconds
        // (i.e. the arrival sensor hasn't notified us), then there's a fault.
        this.timer.schedule(timerTask, 1500);
    }

    public void stopTimer() {
        this.timerTask.cancel();
    }

    public void addDestination(final int floor) {
        this.destinations.add(floor);
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
