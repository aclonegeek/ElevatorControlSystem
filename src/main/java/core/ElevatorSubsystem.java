package core;

public class ElevatorSubsystem implements Runnable {
    private final Scheduler scheduler;
    private final FloorData floorData;
    
    public ElevatorSubsystem(final Scheduler scheduler) {
        this.scheduler = scheduler;
    }
    
    @Override
    public void run() {
        this.scheduler.registerElevatorSubsystem(this);
        while (true) {
            this.floorData = this.scheduler.getFloorEvent();
            this.scheduler.addElevatorEvent(this.floorData);
        }
    }
}
