package floor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalTime;

import elevator.ElevatorFault;
import floor.FloorData.ButtonState;
import junit.framework.TestCase;

public class FloorDataTest extends TestCase {
    // Test writing/reading FloorData to/from a file.
    public void testFileSerialization() {
        final LocalTime time = LocalTime.now();
        final FloorData outputData = new FloorData(2, ButtonState.UP, time, 4, ElevatorFault.ELEVATOR_STUCK, 4);
        try {
            // Serialize FloorData to file.
            final FileOutputStream fout = new FileOutputStream("floorDataTest.txt");
            final ObjectOutputStream out = new ObjectOutputStream(fout);
            out.writeObject(outputData);
            out.flush();
            out.close();

            // Deserialize FloorData from file.
            final ObjectInputStream in = new ObjectInputStream(new FileInputStream("floorDataTest.txt"));
            final FloorData inputData = (FloorData) in.readObject();
            assertEquals(outputData.getFloor(), inputData.getFloor());
            assertEquals(outputData.getButtonState(), inputData.getButtonState());
            assertEquals(outputData.getTime(), inputData.getTime());
            assertEquals(outputData.getDestination(), inputData.getDestination());
            in.close();

            // Delete file.
            final File file = new File("floorDataTest.txt");
            file.delete();
        } catch (final Exception e) {
            System.err.println(e);

            // Force a test failure if fall into catch block.
            assertTrue(false);
        }
    }
}
