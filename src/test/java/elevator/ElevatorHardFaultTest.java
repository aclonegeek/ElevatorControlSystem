package elevator;

import classes.RunnableElevatorSystem;
import classes.RunnableFloorSystem;
import classes.RunnableScheduler;
import global.Globals;
import junit.framework.TestCase;

//NOTE: Methods must be run individually due to socket binding issues.
public class ElevatorHardFaultTest extends TestCase {
    /*
     * Elevator gets stuck on floor 3 on its way to floor 5 then 7. Verify that the
     * elevator has stopped at floor 3 and a different elevator is sent.
     */
    public void testElevatorStuckFault() {
        // Create Scheduler and ElevatorSystem.
        final RunnableScheduler runnableScheduler = new RunnableScheduler();
        new Thread(runnableScheduler).start();

        final RunnableElevatorSystem runnableElevatorSystem =
                new RunnableElevatorSystem(Globals.MAX_ELEVATORS);
        new Thread(runnableElevatorSystem).start();

        // Create FloorSystem (this will begin sending requests).
        final RunnableFloorSystem runnableFloorSystem = new RunnableFloorSystem("/elevatorStuckFaultData.txt", 10);
        new Thread(runnableFloorSystem).start();

        Globals.sleep(20000);
        assertEquals(3, runnableElevatorSystem.getElevatorSystem().getElevators().get(3).getSubsystem()
                .getCurrentFloor());
        assertEquals(7, runnableElevatorSystem.getElevatorSystem().getElevators().get(2).getSubsystem()
                .getCurrentFloor());
    }

    /*
     * Arrival sensor fails on floor 3 when an elevator is on its way to floor 5
     * then 7. Verify that the elevator has stopped at floor 3 and a different
     * elevator is sent.
     */
    public void testSensorFault() {
        // Create Scheduler and ElevatorSystem.
        final RunnableScheduler runnableScheduler = new RunnableScheduler();
        new Thread(runnableScheduler).start();

        final RunnableElevatorSystem runnableElevatorSystem =
                new RunnableElevatorSystem(Globals.MAX_ELEVATORS);
        new Thread(runnableElevatorSystem).start();

        // Create FloorSystem (this will begin sending requests).
        final RunnableFloorSystem runnableFloorSystem = new RunnableFloorSystem("/sensorFaultData.txt", 10);
        new Thread(runnableFloorSystem).start();

        Globals.sleep(20000);
        assertEquals(3, runnableElevatorSystem.getElevatorSystem().getElevators().get(3).getSubsystem()
                .getCurrentFloor());
        assertEquals(7, runnableElevatorSystem.getElevatorSystem().getElevators().get(2).getSubsystem()
                .getCurrentFloor());
    }
}
