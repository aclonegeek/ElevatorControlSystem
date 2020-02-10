package core;

import junit.framework.TestCase;

public class SchedulerTest extends TestCase {
    // Tests that FloorData objects pass through Scheduler the correct number of
    // times.
    public void testScheduler() {
        final Scheduler scheduler = new Scheduler();
        final FloorSubsystem floorSubsystem = new FloorSubsystem(scheduler, 1);
        final Elevator elevator = new Elevator(scheduler);

        new Thread(scheduler).start();
        new Thread(floorSubsystem).start();
        new Thread(elevator).start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.err.println(e);
        }

        assertEquals(16, scheduler.getFloorDataCount());
    }
}
