package core;

import junit.framework.TestCase;

public class SystemTest extends TestCase {
    public void testSystem() {
        Scheduler scheduler = new Scheduler();
        FloorSubsystem floorSubsystem = new FloorSubsystem(scheduler, 1);
        ElevatorSubsystem elevatorSubsystem = new ElevatorSubsystem(scheduler);
        
        Thread schedulerThread = new Thread(scheduler);
        Thread floorSubsystemThread = new Thread(floorSubsystem);
        Thread elevatorSubsystemThread = new Thread(elevatorSubsystem);
        
        schedulerThread.start();
        floorSubsystemThread.start();
        elevatorSubsystemThread.start(); 
    }
}
