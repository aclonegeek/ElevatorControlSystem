package floor;

import java.time.LocalTime;

import elevator.Elevator;
import floor.FloorSubsystem;
import junit.framework.TestCase;
import scheduler.Scheduler;

public class FloorSubsystemTest extends TestCase {
    // Test that pressing the UP button brings the elevator to the correct floor.
    public void testUpButton() {
        final int elevatorId = 0;
        final Scheduler scheduler = new Scheduler();
        final Elevator elevator = new Elevator(elevatorId, scheduler);
        final FloorSubsystem floor = new FloorSubsystem(scheduler, 5);
        scheduler.registerElevator(elevatorId);

        new Thread(scheduler).start();
        new Thread(elevator).start();
        new Thread(floor).start();
        this.sleep(100);

        // Verify elevator is initially on ground floor (0).
        assertEquals(0, elevator.getSubsystem().getCurrentFloor());

        // Verify elevator moves up to fifth floor when the UP button is pressed.
        final FloorData floorData =
                new FloorData(floor.getFloor(), FloorData.ButtonState.UP, LocalTime.now());
        floor.addFloorRequest(floorData);
        this.sleep(100);
        assertEquals(5, elevator.getSubsystem().getCurrentFloor());
    }

    // Test that pressing the DOWN button brings the elevator to the correct floor.
    public void testDownButton() {
        final int elevatorId = 0;
        final Scheduler scheduler = new Scheduler();
        final Elevator elevator = new Elevator(elevatorId, scheduler);
        final FloorSubsystem floor = new FloorSubsystem(scheduler, 5);
        scheduler.registerElevator(elevatorId);

        new Thread(scheduler).start();
        new Thread(elevator).start();
        new Thread(floor).start();
        this.sleep(100);

        // Verify elevator is initially on ground floor (0).
        assertEquals(0, elevator.getSubsystem().getCurrentFloor());

        // Verify elevator moves up to fifth floor when the UP button is pressed.
        final FloorData floorData =
                new FloorData(floor.getFloor(), FloorData.ButtonState.DOWN, LocalTime.now());
        floor.addFloorRequest(floorData);
        this.sleep(100);
        assertEquals(5, elevator.getSubsystem().getCurrentFloor());
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            System.err.println(e);
        }
    }
}
