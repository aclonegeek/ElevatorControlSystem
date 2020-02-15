package elevator;

import java.time.LocalTime;

import floor.FloorData;
import floor.FloorSubsystem;
import junit.framework.TestCase;
import scheduler.Scheduler;

public class ElevatorSubsystemTest extends TestCase {
    public void testStates() {
        final int elevatorId = 0;
        final Scheduler scheduler = new Scheduler();
        final Elevator elevator = new Elevator(elevatorId, scheduler);

        new Thread(scheduler).start();
        new Thread(elevator).start();

        // Verify initial state of the elevator (idle with door open).
        this.sleep(100);
        assertEquals(ElevatorState.IDLE_DOOR_OPEN, elevator.getSubsystem().getState());

        // Verify state after closing elevator door.
        final ElevatorData closeDoorData =
                new ElevatorData(elevatorId, elevator.getSubsystem().getCurrentFloor(), LocalTime.now());
        final ElevatorEvent closeDoorEvent = new ElevatorEvent(closeDoorData, ElevatorAction.CLOSE_DOORS);
        scheduler.addElevatorEvent(closeDoorEvent);
        this.sleep(100);
        assertEquals(ElevatorState.IDLE_DOOR_CLOSED, elevator.getSubsystem().getState());

        // Verify state while moving up.
        final ElevatorData moveUpData =
                new ElevatorData(elevatorId, elevator.getSubsystem().getCurrentFloor(), LocalTime.now());
        final ElevatorEvent moveUpEvent = new ElevatorEvent(moveUpData, ElevatorAction.MOVE_UP);
        scheduler.addElevatorEvent(moveUpEvent);
        this.sleep(100);
        assertEquals(ElevatorState.MOVING_UP, elevator.getSubsystem().getState());

        // Verify state while moving down.
        final ElevatorData moveDownData =
                new ElevatorData(elevatorId, elevator.getSubsystem().getCurrentFloor(), LocalTime.now());
        final ElevatorEvent moveDownEvent = new ElevatorEvent(moveDownData, ElevatorAction.MOVE_DOWN);
        scheduler.addElevatorEvent(moveDownEvent);
        this.sleep(100);
        assertEquals(ElevatorState.MOVING_DOWN, elevator.getSubsystem().getState());

        // Verify state after stopping elevator.
        final ElevatorData stopData =
                new ElevatorData(elevatorId, elevator.getSubsystem().getCurrentFloor(), LocalTime.now());
        final ElevatorEvent stopEvent = new ElevatorEvent(stopData, ElevatorAction.STOP_MOVING);
        scheduler.addElevatorEvent(stopEvent);
        this.sleep(100);
        assertEquals(ElevatorState.IDLE_DOOR_CLOSED, elevator.getSubsystem().getState());

        // Verify state after opening elevator door.
        final ElevatorData openDoorData =
                new ElevatorData(elevatorId, elevator.getSubsystem().getCurrentFloor(), LocalTime.now());
        final ElevatorEvent openDoorEvent = new ElevatorEvent(openDoorData, ElevatorAction.OPEN_DOORS);
        scheduler.addElevatorEvent(openDoorEvent);
        this.sleep(100);
        assertEquals(ElevatorState.IDLE_DOOR_OPEN, elevator.getSubsystem().getState());
    }

    public void testMovement() {
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

        // Verify elevator is initially on ground floor (0).
        assertEquals(0, elevator.getSubsystem().getCurrentFloor());

        // Verify elevator moves up to first floor when UP button is pressed on first floor.
        final FloorData floor1Data = new FloorData(floor1.getFloor(), FloorData.ButtonState.UP, LocalTime.now());
        floor1.addFloorRequest(floor1Data);
        this.sleep(100);
        assertEquals(1, elevator.getSubsystem().getCurrentFloor());

        // Verify elevator moves up to second floor when DOWN button is pressed on second floor.
        final FloorData floor2Data = new FloorData(floor2.getFloor(), FloorData.ButtonState.DOWN, LocalTime.now());
        floor2.addFloorRequest(floor2Data);
        this.sleep(100);
        assertEquals(2, elevator.getSubsystem().getCurrentFloor());

        // Verify elevator moves back down to ground floor (0) when UP button is pressed on ground floor.
        final FloorData floor0Data = new FloorData(floor0.getFloor(), FloorData.ButtonState.UP, LocalTime.now());
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
