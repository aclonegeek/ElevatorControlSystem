package elevator;

import junit.framework.TestCase;
import scheduler.Scheduler;

import java.util.ArrayList;

import floor.FloorSystem;
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

        // Create FloorSystem (this will begin sending requests).
        final RunnableFloorSystem runnableFloorSystem =
                new RunnableFloorSystem("/elevatorSystemTestData.txt", 10);
        new Thread(runnableFloorSystem).start();

        Globals.sleep(10000);

        // Verify each Elevator ends up at the correct floor and in the correct state.
        assertEquals(4, elevators.get(0).getSubsystem().getCurrentFloor());
        assertEquals(7, elevators.get(1).getSubsystem().getCurrentFloor());
        assertEquals(12, elevators.get(2).getSubsystem().getCurrentFloor());
        assertEquals(9, elevators.get(3).getSubsystem().getCurrentFloor());
    }

    private class RunnableScheduler implements Runnable {
        final Scheduler scheduler;

        public RunnableScheduler() {
            this.scheduler = new Scheduler();
        }

        public void run() {
            this.scheduler.run();
        }
    }

    private class RunnableElevatorSystem implements Runnable {
        final ElevatorSystem elevatorSystem;

        public RunnableElevatorSystem(final int numOfElevators) {
            this.elevatorSystem = new ElevatorSystem(numOfElevators);
        }

        public void run() {
            this.elevatorSystem.run();
        }

        public ElevatorSystem getElevatorSystem() {
            return this.elevatorSystem;
        }
    }

    private class RunnableFloorSystem implements Runnable {
        final FloorSystem floorSystem;

        public RunnableFloorSystem(final String filename, final int sleepTime) {
            this.floorSystem = new FloorSystem(filename, sleepTime);
        }

        public void run() {
            this.floorSystem.run();
        }
    }
}
