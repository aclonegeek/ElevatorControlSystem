package core;

public class ElevatorSubsystem implements Runnable {
    private final Scheduler scheduler;

    public ElevatorSubsystem(final Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void run() {
        this.scheduler.registerElevatorSubsystem(this);
        while (true) {
            FloorData floorData = this.scheduler.getFloorEvent();
            this.scheduler.addElevatorEvent(floorData);
        }
    }
}
