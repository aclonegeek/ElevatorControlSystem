package elevator;

import global.Globals;
import junit.framework.TestCase;

public class ElevatorSubsystemTest extends TestCase {
    public void testStates() {
        final ElevatorSystem elevatorSystem = new ElevatorSystem(1);
        final ElevatorSubsystem elevatorSubsystem = new ElevatorSubsystem(1, elevatorSystem);

        new Thread(elevatorSubsystem).start();

        // Verify initial state (idle with door open).
        assertEquals(ElevatorState.IDLE_DOOR_OPEN, elevatorSubsystem.getState());
        
        // Verify state after closing door.
        elevatorSubsystem.updateState(ElevatorAction.CLOSE_DOORS);
        assertEquals(ElevatorState.DOOR_CLOSED_FOR_MOVING, elevatorSubsystem.getState());
        
        // Verify state while moving up.
        elevatorSubsystem.updateState(ElevatorAction.MOVE_UP);
        assertEquals(ElevatorState.MOVING_UP, elevatorSubsystem.getState());
        
        // Verify state after stopping.
        elevatorSubsystem.updateState(ElevatorAction.STOP_MOVING);
        assertEquals(ElevatorState.DOOR_CLOSED_FOR_IDLING, elevatorSubsystem.getState());
        
        // Verify state after opening doors.
        elevatorSubsystem.updateState(ElevatorAction.OPEN_DOORS);
        assertEquals(ElevatorState.IDLE_DOOR_OPEN, elevatorSubsystem.getState());
        
        elevatorSystem.closeSockets();
    }

    public void testMovement() {
        final ElevatorSystem elevatorSystem = new ElevatorSystem(1);
        final ElevatorSubsystem elevatorSubsystem = new ElevatorSubsystem(1, elevatorSystem);

        new Thread(elevatorSubsystem).start();
        
        // Close elevators doors and move up.
        elevatorSubsystem.updateState(ElevatorAction.CLOSE_DOORS);
        elevatorSubsystem.updateState(ElevatorAction.MOVE_UP);
        
        // Verify the elevator has moved up.
        final int height1 = elevatorSubsystem.getCurrentHeight();
        Globals.sleep(5000);
        assertTrue(elevatorSubsystem.getCurrentHeight() > height1);
        
        // Stop the elevator and move down.
        elevatorSubsystem.updateState(ElevatorAction.STOP_MOVING);
        elevatorSubsystem.updateState(ElevatorAction.MOVE_DOWN);
        
        // Verify the elevator has moved down.
        final int height2 = elevatorSubsystem.getCurrentHeight();
        Globals.sleep(5000);
        assertTrue(elevatorSubsystem.getCurrentHeight() < height2);
        
        elevatorSystem.closeSockets();
    }
}
