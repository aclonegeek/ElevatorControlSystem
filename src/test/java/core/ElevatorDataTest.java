package core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalTime;

import junit.framework.TestCase;

public class ElevatorDataTest extends TestCase {
    // Test writing/reading ElevatorData to/from a file.
    public void testFileSerialization() {
        final LocalTime time = LocalTime.now();
        final ElevatorData outputData = new ElevatorData(1, 2, 3, time);
        try {
            // Serialize ElevatorData to file.
            final FileOutputStream fout = new FileOutputStream("elevatorDataTest.txt");
            final ObjectOutputStream out = new ObjectOutputStream(fout);
            out.writeObject(outputData);
            out.flush();
            out.close();

            // Deserialize ElevatorData from file.
            final ObjectInputStream in = new ObjectInputStream(new FileInputStream("elevatorDataTest.txt"));
            final ElevatorData inputData = (ElevatorData) in.readObject();
            assertEquals(outputData.getElevatorId(), inputData.getElevatorId());
            assertEquals(outputData.getCurrentFloor(), inputData.getCurrentFloor());
            assertEquals(outputData.getDestinationFloor()(), inputData.getDestinationFloor());
            assertEquals(outputData.getTime(), inputData.getTime());
            in.close();

            // Delete file.
            final File file = new File("elevatorDataTest.txt");
            file.delete();
        } catch (Exception e) {
            System.err.println(e);

            // Force a test failure if fall into catch block.
            assertTrue(false);
        }
    }
}