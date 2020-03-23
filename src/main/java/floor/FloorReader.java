package floor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import elevator.ElevatorFault;
import floor.FloorData;
import floor.FloorData.ButtonState;

/**
 * Handles reading files following the floor subsystem format.
 */
public class FloorReader {
    /**
     * Reads an input file following the floor subsystem format into a list of
     * serializable objects.
     *
     * @param path the path to the file
     *
     * @return a list of FloorData objects
     */
    public ArrayList<FloorData> readFile(final String path) {
        final ArrayList<FloorData> floorRequests = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(new File(path)))) {
            String line;

            while ((line = br.readLine()) != null) {
                final String[] data = line.split(" ");
                final LocalTime localTime =
                        LocalTime.parse(data[0], DateTimeFormatter.ofPattern("HH:mm:ss.S"));
                final int floorNumber = Integer.parseInt(data[1]);
                final ButtonState buttonState = ButtonState.valueOf(data[2]);
                final int destination = Integer.parseInt(data[3]);

                ElevatorFault elevatorFault = null;
                Integer elevatorFaultFloor = null;
                if (data.length > 4) {
                    switch (data[4]) {
                    case "ESF":
                        elevatorFault = ElevatorFault.ELEVATOR_STUCK;
                        break;
                    case "SF":
                        elevatorFault = ElevatorFault.SENSOR_ERROR;
                        break;
                    case "DSOF":
                        elevatorFault = ElevatorFault.DOOR_STUCK_OPEN;
                        break;
                    case "DSCF":
                        elevatorFault = ElevatorFault.DOOR_STUCK_CLOSED;
                        break;
                    default:
                        break;
                    }

                    elevatorFaultFloor = Integer.parseInt(data[5]);
                }

                floorRequests.add(new FloorData(floorNumber, buttonState, localTime, destination, elevatorFault, elevatorFaultFloor));
            }
        } catch (final FileNotFoundException f) {
            System.err.println(f);
        } catch (final IOException e) {
            System.err.println(e);
        }

        return floorRequests;
    }
}
