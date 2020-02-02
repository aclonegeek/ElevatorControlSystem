package core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import core.FloorData;

import core.FloorData.ButtonState;

/**
 * Reads an input file following the floor subsystem format into a list of
 * serializable objects.
 */
public class FloorReader {
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

                floorRequests.add(new FloorData(1, floorNumber, buttonState, localTime));
            }
        } catch (FileNotFoundException f) {
            System.err.println(f);
        } catch (IOException e) {
            System.err.println(e);
        }

        return floorRequests;
    }
}
