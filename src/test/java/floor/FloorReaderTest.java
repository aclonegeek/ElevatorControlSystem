package floor;

import java.time.LocalTime;
import java.util.ArrayList;

import floor.FloorData.ButtonState;
import junit.framework.TestCase;

public class FloorReaderTest extends TestCase {
    // Tests that the floor reader creates FloorData objects with the correct data.
    public void testFloorReader() {
        final FloorReader floorReader = new FloorReader();
        final String path = this.getClass().getResource("/data.txt").getFile();
        final ArrayList<FloorData> floorData = floorReader.readFile(path);

        assertEquals(2, floorData.get(0).getFloor());
        assertEquals(ButtonState.UP, floorData.get(0).getButtonState());
        assertEquals(LocalTime.parse("14:05:15.500"), floorData.get(0).getTime());
        assertEquals(4, floorData.get(0).getDestination());

        assertEquals(3, floorData.get(1).getFloor());
        assertEquals(ButtonState.UP, floorData.get(1).getButtonState());
        assertEquals(LocalTime.parse("18:25:35.600"), floorData.get(1).getTime());
        assertEquals(7, floorData.get(1).getDestination());
    }
}
