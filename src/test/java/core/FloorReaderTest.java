package core;

import java.time.LocalTime;
import java.util.ArrayList;

import core.FloorData.ButtonState;
import junit.framework.TestCase;

public class FloorReaderTest extends TestCase {
    public void testFloorReader() {
        final FloorReader floorReader = new FloorReader();
        final String path = this.getClass().getResource("/floorData.txt").getFile();
        final ArrayList<FloorData> floorData = floorReader.readFile(path);

        assertEquals(1, floorData.get(0).getElevatorId());
        assertEquals(2, floorData.get(0).getFloorNumber());
        assertEquals(ButtonState.UP, floorData.get(0).getButtonState());
        assertEquals(LocalTime.parse("14:05:15.500"), floorData.get(0).getTime());

        assertEquals(1, floorData.get(1).getElevatorId());
        assertEquals(8, floorData.get(1).getFloorNumber());
        assertEquals(ButtonState.DOWN, floorData.get(1).getButtonState());
        assertEquals(LocalTime.parse("18:25:35.600"), floorData.get(1).getTime());
    }
}
