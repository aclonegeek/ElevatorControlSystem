package elevator;

import java.util.ArrayList;

import global.Globals;
import junit.framework.TestCase;

public class ArrivalSensorTest extends TestCase {
    public void testArrivalSensors() {
        final ElevatorSystem elevatorSystem = new ElevatorSystem(1);
        final Elevator elevator = new Elevator(1, elevatorSystem);
        final ArrayList<ArrivalSensor> arrivalSensors = elevator.getArrivalSensors();

        // Verify Elevator is only detected by ArrivalSensors it passes by.
        // It starts at floor 10 and moves upwards approx. 5 floors.
        elevator.getSubsystem().setCurrentHeight(Globals.FLOOR_HEIGHT * 10);
        elevator.getSubsystem().updateState(ElevatorAction.CLOSE_DOORS);
        elevator.getSubsystem().updateState(ElevatorAction.MOVE_UP);
        Globals.sleep(5000);
        elevator.getSubsystem().updateState(ElevatorAction.STOP_MOVING);
        
        for (int i = 0; i < 10; i++) {
            assertFalse(arrivalSensors.get(i).hasDetectedElevator());
        }
        
        for (int i = 10; i < 15; i++) {
            assertTrue(arrivalSensors.get(i).hasDetectedElevator());
        }
        
        elevatorSystem.closeSockets();
        Globals.sleep(500);
    }
}
