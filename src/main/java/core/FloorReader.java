package core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import core.FloorData;

import core.FloorData.ButtonState;

/**
 * @summary Floor subsystem to read an input file using the format shown in project specifications.
 */
public class FloorReader {
    //stores the queue of people requesting to get on the elevator of type FloorData.
    private final ArrayList<FloorData> floorRequests; 
    
    public FloorReader(final String path){ 
        floorRequests = new ArrayList<FloorData>();
        try {
            this.readFile(path);
        } catch (Exception e) {
            System.err.println(e);
        }
    }
    
    // store input file into an ArrayList of type FloorData.
    private void readFile(final String path) throws Exception {
        final File file = new File(path);
        final BufferedReader br = new BufferedReader(new FileReader(file)); 
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.S");
        String line; 
        
        while ((line = br.readLine()) != null) { 
          final String[] data = line.split(" "); 
          final LocalTime localTime = LocalTime.parse(data[0], formatter);
          final int floorNumber = Integer.parseInt(data[1]);
          final ButtonState buttonState = ButtonState.valueOf(data[2]);
          
          final FloorData floorData = new FloorData(1, floorNumber, buttonState, localTime);
          floorRequests.add(floorData);                    
        } 
        br.close();
    }
    
    //getter to get stored ArrayList of FloorData (represents list of floors in text file).
    public ArrayList<FloorData> getFloorRequests() {
        return this.floorRequests;
    }
    
}
