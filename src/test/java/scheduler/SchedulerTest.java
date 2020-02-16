package scheduler;

import java.time.LocalTime;

import elevator.Elevator;
import floor.FloorData;
import floor.FloorSubsystem;
import junit.framework.TestCase;
import scheduler.Scheduler;
import scheduler.Scheduler.SchedulerState;

public class SchedulerTest extends TestCase {
    public void testScheduler() {
        final int elevatorId = 0;
        final Scheduler scheduler = new Scheduler();
        final Elevator elevator = new Elevator(elevatorId, scheduler);
        final FloorSubsystem floor0 = new FloorSubsystem(scheduler, 0);
        final FloorSubsystem floor1 = new FloorSubsystem(scheduler, 1);
        final FloorSubsystem floor2 = new FloorSubsystem(scheduler, 2);
        scheduler.registerElevator(elevatorId);

        new Thread(scheduler).start();
        new Thread(elevator).start();
        new Thread(floor0).start();
        new Thread(floor1).start();
        new Thread(floor2).start();
        this.sleep(100);

        assertEquals(SchedulerState.WAITING, scheduler.getState());

        // Verify elevator is initially on ground floor (0).
        assertEquals(0, elevator.getSubsystem().getCurrentFloor());

        // Verify elevator moves up to first floor when UP button is pressed on first floor.
        final FloorData floor1Data = new FloorData(floor1.getFloor(),
                                                   FloorData.ButtonState.UP,
                                                   LocalTime.now(),
                                                   floor1.getFloor());
        floor1.addFloorRequest(floor1Data);
        this.sleep(100);
        assertEquals(1, elevator.getSubsystem().getCurrentFloor());

        // Verify elevator moves up to second floor when DOWN button is pressed on second floor.
        final FloorData floor2Data = new FloorData(floor2.getFloor(),
                                                   FloorData.ButtonState.DOWN,
                                                   LocalTime.now(),
                                                   floor2.getFloor());
        floor2.addFloorRequest(floor2Data);
        this.sleep(100);
        assertEquals(2, elevator.getSubsystem().getCurrentFloor());

        // Verify elevator moves back down to ground floor (0) when UP button is pressed on ground floor.
        final FloorData floor0Data = new FloorData(floor0.getFloor(),
                                                   FloorData.ButtonState.UP,
                                                   LocalTime.now(),
                                                   floor0.getFloor());
        floor0.addFloorRequest(floor0Data);
        this.sleep(100);
        assertEquals(0, elevator.getSubsystem().getCurrentFloor());
    }

    private void sleep(final int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            System.err.println(e);
        }
    }
}
