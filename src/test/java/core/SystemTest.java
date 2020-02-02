package core;

import junit.framework.TestCase;

public class SystemTest extends TestCase {
    public void testSystem() {
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
        assertEquals(4, elevatorSubsystem.getFloorDataCount());
    }
}
