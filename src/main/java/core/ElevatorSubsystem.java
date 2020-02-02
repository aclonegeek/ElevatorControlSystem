package core;

public class ElevatorSubsystem implements Runnable {
    private final Scheduler scheduler;
    
    // Used for testing purposes.
    private int floorDataCount;

    public ElevatorSubsystem(final Scheduler scheduler) {
        this.scheduler = scheduler;
        this.floorDataCount = 0;
    }

    @Override
    public void run() {
        this.scheduler.registerElevatorSubsystem(this);
        while (true) {
            FloorData floorData = this.scheduler.removeFloorEvent();
            this.floorDataCount++;
            this.scheduler.addElevatorEvent(floorData);
        }
    }
    
    public int getFloorDataCount() {
        return this.floorDataCount;
    }
}
