package elevator;

import classes.RunnableElevatorSystem;
import classes.RunnableFloorSystem;
import classes.RunnableScheduler;
import global.Globals;
import junit.framework.TestCase;

// NOTE: Methods must be run individually due to socket binding issues.
public class ElevatorSoftFaultTest extends TestCase {
    /*
     * Elevator receives two requests to go to floor 5: one from floor 2 and one
     * from floor 4. On floor 4, the elevator's door gets stuck open so
     * ElevatorSystem does not send Scheduler back a STATE_CHANGED response. The
     * Scheduler notices this so sends another CLOSE_DOORS action.
     */
    public void testDoorStuckOpenFault() {
        // Create Scheduler and ElevatorSystem.
        final RunnableScheduler runnableScheduler = new RunnableScheduler();
        new Thread(runnableScheduler).start();

        final RunnableElevatorSystem runnableElevatorSystem =
                new RunnableElevatorSystem(Globals.MAX_ELEVATORS);
        new Thread(runnableElevatorSystem).start();

        // Create FloorSystem (this will begin sending requests).
        final RunnableFloorSystem runnableFloorSystem =
                new RunnableFloorSystem("/doorStuckOpenFaultData.txt", 10);
        new Thread(runnableFloorSystem).start();

        Globals.sleep(15000);
        assertEquals(5, runnableElevatorSystem.getElevatorSystem().getElevators().get(0).getSubsystem().getCurrentFloor());
    }

    /*
     * Elevator receives two requests to go to floor 5: one from floor 2 and one
     * from floor 4. On floor 4, the elevator's door gets stuck closed so
     * ElevatorSystem does not send Scheduler back a STATE_CHANGED response. The
     * Scheduler notices this so sends another OPEN_DOORS action.
     */
    public void testDoorStuckClosedFault() {
        // Create Scheduler and ElevatorSystem.
        final RunnableScheduler runnableScheduler = new RunnableScheduler();
        new Thread(runnableScheduler).start();

        final RunnableElevatorSystem runnableElevatorSystem =
                new RunnableElevatorSystem(Globals.MAX_ELEVATORS);
        new Thread(runnableElevatorSystem).start();

        // Create FloorSystem (this will begin sending requests).
        final RunnableFloorSystem runnableFloorSystem =
                new RunnableFloorSystem("/doorStuckClosedFaultData.txt", 10);
        new Thread(runnableFloorSystem).start();

        Globals.sleep(15000);
        assertEquals(5, runnableElevatorSystem.getElevatorSystem().getElevators().get(0).getSubsystem().getCurrentFloor());
    }
}
