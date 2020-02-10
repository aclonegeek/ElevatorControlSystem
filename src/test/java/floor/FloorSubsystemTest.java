package floor;

import elevator.Elevator;
import floor.FloorSubsystem;
import junit.framework.TestCase;
import scheduler.Scheduler;

public class FloorSubsystemTest extends TestCase {
    // Tests that FloorData objects pass through FloorSubsystem the correct number
    // of times.
    public void testFloorSubsystem() {
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

        assertEquals(4, floorSubsystem.getFloorDataCount());
    }
}
