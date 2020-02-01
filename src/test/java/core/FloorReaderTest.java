package core;

import java.time.LocalTime;
import core.FloorData.ButtonState;
import junit.framework.TestCase;

/**
 * @summary Iteration 1 - Tester to determine if FloorReader is working correclty
 */

public class FloorReaderTest extends TestCase {

    public void testFloorReader() {
        final FloorReader floorReader = new FloorReader("./FloorData.txt");
        FloorData floorData = floorReader.getFloorRequests().get(0);
        
        assertEquals(1, floorData.getElevatorId());
        assertEquals(2, floorData.getFloorNumber());
        assertEquals(ButtonState.UP,floorData.getButtonState());
        assertEquals(LocalTime.parse("14:05:15.500"),floorData.getTime());
        
        floorData = floorReader.getFloorRequests().get(1);
        assertEquals(1, floorData.getElevatorId());
        assertEquals(8, floorData.getFloorNumber());
        assertEquals(ButtonState.DOWN,floorData.getButtonState());
        assertEquals(LocalTime.parse("18:25:35.600"),floorData.getTime()); 
    }
}
