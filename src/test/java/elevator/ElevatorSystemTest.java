package elevator;

import junit.framework.TestCase;
import scheduler.Scheduler;

import java.util.ArrayList;

import classes.RunnableElevatorSystem;
import classes.RunnableFloorSystem;
import classes.RunnableScheduler;
import global.Globals;

public class ElevatorSystemTest extends TestCase {
    public void testElevatorSystem() {
        // Create Scheduler and ElevatorSystem.
        final RunnableScheduler runnableScheduler = new RunnableScheduler();
        new Thread(runnableScheduler).start();

        final RunnableElevatorSystem runnableElevatorSystem =
                new RunnableElevatorSystem(Globals.MAX_ELEVATORS);
        new Thread(runnableElevatorSystem).start();

        // Move Elevators to different floors.
        final ArrayList<Elevator> elevators = runnableElevatorSystem.getElevatorSystem().getElevators();
        elevators.get(0).getSubsystem().setCurrentHeight(0);
        elevators.get(1).getSubsystem().setCurrentHeight(Globals.FLOOR_HEIGHT * 5);
        elevators.get(2).getSubsystem().setCurrentHeight(Globals.FLOOR_HEIGHT * 10);
        elevators.get(3).getSubsystem().setCurrentHeight(Globals.FLOOR_HEIGHT * 15);
        
        Globals.sleep(1000);
        
        final Scheduler scheduler = runnableScheduler.getScheduler();
        scheduler.setElevatorStatusFloor(elevators.get(0).getSubsystem().getElevatorId(), 0);
        scheduler.setElevatorStatusFloor(elevators.get(1).getSubsystem().getElevatorId(), 5);
        scheduler.setElevatorStatusFloor(elevators.get(2).getSubsystem().getElevatorId(), 10);
        scheduler.setElevatorStatusFloor(elevators.get(3).getSubsystem().getElevatorId(), 15);

        // Create FloorSystem (this will begin sending requests).
        final RunnableFloorSystem runnableFloorSystem =
                new RunnableFloorSystem("/elevatorSystemTestData.txt", 10);
        new Thread(runnableFloorSystem).start();

        Globals.sleep(10000);

        // Verify each Elevator ends up at the correct floor.
        // Elevator 1 at floor 0 moved UP to floor 2, then UP to its destination at floor 4.
        assertEquals(4, elevators.get(0).getSubsystem().getCurrentFloor());
        
        // Elevator 2 at floor 5 moved DOWN to floor 4, then UP to its destination at floor 6.
        assertEquals(6, elevators.get(1).getSubsystem().getCurrentFloor());
        
        // Elevator 3 at floor 10 moved UP to floor 12, then DOWN to its destination at floor 9.
        assertEquals(9, elevators.get(2).getSubsystem().getCurrentFloor());
        
        // Elevator 4 at floor 15 moved DOWN to floor 13, then DOWN to its destination at floor 10.
        assertEquals(10, elevators.get(3).getSubsystem().getCurrentFloor());
    }
}
