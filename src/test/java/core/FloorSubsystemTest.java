package core;

import junit.framework.TestCase;

public class FloorSubsystemTest extends TestCase {
    // Tests that FloorData objects pass through FloorSubsystem the correct number of times
    public void testScheduler() {
        Scheduler scheduler = new Scheduler();
        FloorSubsystem floorSubsystem = new FloorSubsystem(scheduler, 1);
        ElevatorSubsystem elevatorSubsystem = new ElevatorSubsystem(scheduler);
        
        new Thread(scheduler).start();
        new Thread(floorSubsystem).start();
        new Thread(elevatorSubsystem).start();
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.err.println(e);
        }
        
        assertEquals(4, floorSubsystem.getFloorDataCount());
    }
}
