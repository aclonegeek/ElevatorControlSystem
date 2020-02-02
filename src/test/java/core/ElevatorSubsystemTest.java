package core;

import junit.framework.TestCase;

public class ElevatorSubsystemTest extends TestCase {
    // Tests that FloorData objects pass through ElevatorSubsystem the correct number of times.
    public void testElevatorSubsystem() {
        final Scheduler scheduler = new Scheduler();
        final FloorSubsystem floorSubsystem = new FloorSubsystem(scheduler, 1);
        final ElevatorSubsystem elevatorSubsystem = new ElevatorSubsystem(scheduler);
        
        new Thread(scheduler).start();
        new Thread(floorSubsystem).start();
        new Thread(elevatorSubsystem).start();
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.err.println(e);
        }
        
        assertEquals(4, elevatorSubsystem.getFloorDataCount());
    }
}
