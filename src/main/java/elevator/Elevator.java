package elevator;

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
        final int id = elevatorSubsystem.getElevatorId();
        this.scheduler.registerElevator(id);
        while (true) {
            // Receive ElevatorEvent from Scheduler, update state, and return response to
            // the Scheduler.
            final ElevatorAction elevatorAction = this.scheduler.getElevatorAction(id);
            final ElevatorResponse response = this.elevatorSubsystem.updateState(elevatorAction);
            this.scheduler.handleElevatorResponse(id, response);
        }
    }

    public ElevatorSubsystem getSubsystem() {
        return this.elevatorSubsystem;
    }
}
