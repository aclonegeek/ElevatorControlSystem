package core;

public class Elevator implements Runnable {
    private final ElevatorSubsystem elevatorSubsystem;
    private final Scheduler scheduler;

    // Used for testing purposes.
    private int floorDataCount;

    public Elevator(final Scheduler scheduler) {
        this.elevatorSubsystem = new ElevatorSubsystem(0);
        this.scheduler = scheduler;
        this.floorDataCount = 0;
    }
    
    @Override
    public void run() {
        while (true) {
            // Receive FloorData from Scheduler
            final FloorData floorData = this.scheduler.removeFloorEvent();
            this.floorDataCount++;
            
            // TODO: Receive ElevatorCommand from Scheduler?
            
            // Update ElevatorSubsystem state
            
            // Send Elevator data back to Scheduler
            
            this.scheduler.addElevatorEvent(floorData);
        }
    }

    public int getFloorDataCount() {
        return this.floorDataCount;
    }
}
