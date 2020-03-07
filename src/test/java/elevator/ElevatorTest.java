package elevator;

import global.Globals;
import junit.framework.TestCase;

public class ElevatorTest extends TestCase {
    public void testProcessDataWithStateChange() {
        final int elevatorId = 1;
        final ElevatorSystem elevatorSystem = new ElevatorSystem(1);
        final Elevator elevator = new Elevator(elevatorId, elevatorSystem);
        
        final byte[] inputData = new byte[3];
        inputData[0] = Globals.FROM_SCHEDULER;
        inputData[1] = (byte) elevatorId;
        inputData[2] = (byte) ElevatorAction.MOVE_UP.ordinal();
        
        byte[] outputData = elevator.processData(inputData);
        
        assertEquals(5, outputData.length);
        assertEquals(Globals.FROM_ELEVATOR, outputData[0]);
        assertEquals(elevatorId, outputData[1]);
        assertEquals(Elevator.Request.STATE_CHANGED.ordinal(), outputData[2]);
        assertEquals(ElevatorState.MOVING_UP.ordinal(), outputData[3]);
        assertEquals(ElevatorResponse.SUCCESS.ordinal(), outputData[4]);
    }
    
    public void testProcessDataWithoutStateChange() {
        final int elevatorId = 1;
        final ElevatorSystem elevatorSystem = new ElevatorSystem(1);
        final Elevator elevator = new Elevator(elevatorId, elevatorSystem);
        
        final byte[] inputData = new byte[3];
        inputData[0] = Globals.FROM_SCHEDULER;
        inputData[1] = (byte) elevatorId;
        inputData[2] = (byte) ElevatorAction.OPEN_DOORS.ordinal();
        
        byte[] outputData = elevator.processData(inputData);
        
        assertEquals(null, outputData);
    }
}
