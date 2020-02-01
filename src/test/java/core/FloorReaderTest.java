package core;

import java.time.LocalTime;
import core.FloorData.ButtonState;
import junit.framework.TestCase;

/**
 * @author Group 8 (implementation by Tan Tran)
 * 
 * @summary Iteration 1 - Tester to determine if FloorReader is working correclty
 * 
 */

public class FloorReaderTest extends TestCase {

    public void testFloorReader() {
        FloorReader floorReader = new FloorReader("./FloorData.txt");
        
        FloorData floorData1 = floorReader.getFloorRequests().get(0);
        
        assertEquals(1 , floorData1.getElevatorId());
        assertEquals(2 , floorData1.getFloorNumber());
        assertEquals(ButtonState.UP ,floorData1.getButtonState());
        assertEquals(LocalTime.parse("14:05:15.500") ,floorData1.getTime());
    }
}
