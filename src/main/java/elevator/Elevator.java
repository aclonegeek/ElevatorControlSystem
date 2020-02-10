package elevator;

import floor.FloorData;
import scheduler.Scheduler;

public class Elevator implements Runnable {
    private final ElevatorSubsystem elevatorSubsystem;
    private final Scheduler scheduler;

    // Used for testing purposes.
    private int elevatorEventCount;

    public Elevator(final int elevatorId, final Scheduler scheduler) {
        this.elevatorSubsystem = new ElevatorSubsystem(elevatorId);
        this.scheduler = scheduler;
        this.elevatorEventCount = 0;
    }
    
    @Override
    public void run() {
        while (true) {
            // Receive FloorData from Scheduler
            final ElevatorEvent elevatorEvent = this.scheduler.removeElevatorEvent();
            this.elevatorEventCount++;
            
            // Update ElevatorSubsystem state
            elevatorSubsystem.updateState(elevatorEvent.getElevatorAction());
            
            // TODO: What to send back to Scheduler?
        }
    }

    public int getElevatorEventCount() {
        return this.elevatorEventCount;
    }
}