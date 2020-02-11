package elevator;

import floor.FloorData;
import scheduler.Scheduler;

public class Elevator implements Runnable {
    private final ElevatorSubsystem elevatorSubsystem;
    private final Scheduler scheduler;

    public Elevator(final int elevatorId, final Scheduler scheduler) {
        this.elevatorSubsystem = new ElevatorSubsystem(elevatorId);
        this.scheduler = scheduler;
    }

    @Override
    public void run() {
        while (true) {
            // Receive ElevatorEvent from Scheduler, update state, and return response to
            // the Scheduler.
            final ElevatorAction elevatorAction = this.scheduler.getElevatorAction();
            ElevatorResponse response = elevatorSubsystem.updateState(elevatorAction);
            this.scheduler.sendElevatorResponse(response);

            // TODO: Implement pressing floor buttons and sending events to Scheduler.
            // TODO: Check each passing floor to see if it should stop.
        }
    }
}
