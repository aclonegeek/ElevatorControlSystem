package core;

/*
 * Elevator subsystem which simulates the functionality of a particular elevator.
 * Communicates with the scheduler through FloorData objects.
 */
public class ElevatorSubsystem implements Runnable {
    private final Scheduler scheduler;

    // Used for testing purposes.
    private int floorDataCount;

    public ElevatorSubsystem(final Scheduler scheduler) {
        this.scheduler = scheduler;
        this.floorDataCount = 0;
    }

    /*
     * Continually attempts to retrieve FloorData events from the Scheduler. For now,
     * immediately returns them to the Scheduler.
     */
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
