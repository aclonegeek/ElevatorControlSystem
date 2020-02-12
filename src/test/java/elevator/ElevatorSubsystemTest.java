package elevator;

import java.time.LocalTime;

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
        this.sleep();
        assertEquals(ElevatorState.IDLE_DOOR_OPEN, elevator.getSubsystem().getState());

        // Verify state after closing elevator door.
        final ElevatorData closeDoorData =
                new ElevatorData(elevatorId, elevator.getSubsystem().getCurrentFloor(), LocalTime.now());
        final ElevatorEvent closeDoorEvent = new ElevatorEvent(closeDoorData, ElevatorAction.CLOSE_DOORS);
        scheduler.addElevatorEvent(closeDoorEvent);
        this.sleep();
        assertEquals(ElevatorState.IDLE_DOOR_CLOSED, elevator.getSubsystem().getState());

        // Verify state while moving up.
        final ElevatorData moveUpData =
                new ElevatorData(elevatorId, elevator.getSubsystem().getCurrentFloor(), LocalTime.now());
        final ElevatorEvent moveUpEvent = new ElevatorEvent(moveUpData, ElevatorAction.MOVE_UP);
        scheduler.addElevatorEvent(moveUpEvent);
        this.sleep();
        assertEquals(ElevatorState.MOVING_UP, elevator.getSubsystem().getState());

        // Verify state while moving down.
        final ElevatorData moveDownData =
                new ElevatorData(elevatorId, elevator.getSubsystem().getCurrentFloor(), LocalTime.now());
        final ElevatorEvent moveDownEvent = new ElevatorEvent(moveDownData, ElevatorAction.MOVE_DOWN);
        scheduler.addElevatorEvent(moveDownEvent);
        this.sleep();
        assertEquals(ElevatorState.MOVING_DOWN, elevator.getSubsystem().getState());
        
        // Verify state after stopping elevator.
        final ElevatorData stopData =
                new ElevatorData(elevatorId, elevator.getSubsystem().getCurrentFloor(), LocalTime.now());
        final ElevatorEvent stopEvent = new ElevatorEvent(stopData, ElevatorAction.STOP_MOVING);
        scheduler.addElevatorEvent(stopEvent);
        this.sleep();
        assertEquals(ElevatorState.IDLE_DOOR_CLOSED, elevator.getSubsystem().getState());

        // Verify state after opening elevator door.
        final ElevatorData openDoorData =
                new ElevatorData(elevatorId, elevator.getSubsystem().getCurrentFloor(), LocalTime.now());
        final ElevatorEvent openDoorEvent = new ElevatorEvent(openDoorData, ElevatorAction.OPEN_DOORS);
        scheduler.addElevatorEvent(openDoorEvent);
        this.sleep();
        assertEquals(ElevatorState.IDLE_DOOR_OPEN, elevator.getSubsystem().getState());
    }
    
    public void testMovement() {
        final int elevatorId = 0;
        final Scheduler scheduler = new Scheduler();
        final Elevator elevator = new Elevator(elevatorId, scheduler);

        new Thread(scheduler).start();
        new Thread(elevator).start();

        // Verify elevator's initial current floor.
        this.sleep();
        final ElevatorData closeDoorData =
                new ElevatorData(elevatorId, elevator.getSubsystem().getCurrentFloor(), LocalTime.now());
        final ElevatorEvent closeDoorEvent = new ElevatorEvent(closeDoorData, ElevatorAction.CLOSE_DOORS);
        scheduler.addElevatorEvent(closeDoorEvent);
        this.sleep();
        assertEquals(0, elevator.getSubsystem().getCurrentFloor());

        // Verify elevator's current floor after moving up one floor.
        final ElevatorData moveUpData =
                new ElevatorData(elevatorId, elevator.getSubsystem().getCurrentFloor(), LocalTime.now());
        final ElevatorEvent moveUpEvent = new ElevatorEvent(moveUpData, ElevatorAction.MOVE_UP);
        scheduler.addElevatorEvent(moveUpEvent);
        this.sleep();
        assertEquals(1, elevator.getSubsystem().getCurrentFloor());
        
        // Verify elevator's current floor after moving up three more floors.
        scheduler.addElevatorEvent(moveUpEvent);
        this.sleep();
        scheduler.addElevatorEvent(moveUpEvent);
        this.sleep();
        scheduler.addElevatorEvent(moveUpEvent);
        this.sleep();
        assertEquals(4, elevator.getSubsystem().getCurrentFloor());
        
        // Verify elevator's current floor after moving down two floors.
        final ElevatorData moveDownData =
                new ElevatorData(elevatorId, elevator.getSubsystem().getCurrentFloor(), LocalTime.now());
        final ElevatorEvent moveDownEvent = new ElevatorEvent(moveDownData, ElevatorAction.MOVE_DOWN);
        scheduler.addElevatorEvent(moveDownEvent);
        this.sleep();
        scheduler.addElevatorEvent(moveDownEvent);
        this.sleep();
        assertEquals(2, elevator.getSubsystem().getCurrentFloor());
    }
    
    private void sleep() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            System.err.println(e);
        }
    }
}
