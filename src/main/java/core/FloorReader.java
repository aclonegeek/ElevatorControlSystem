package core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import core.FloorData;

import core.FloorData.ButtonState;


/**
 * @author Group 8 (implementation by Tan Tran)
 * 
 * @summary Iteration 1 - Implementation of Floor data
 * 
 * Floor subsystem to read an input file using the format shown in project specifications
 * i.e a time stamp, white space, an integer representing the floor on which the passenger 
 * is making a request, white space, a string consisting of either “up” or “down”, 
 * more white space, then an integer representing floor button within the elevator which 
 * is providing service to the passenger.
 */


public class FloorReader {
    
    //Stores the queue of people requesting to get on the elevator of type FloorData
    private ArrayList<FloorData> floorRequests = new ArrayList<FloorData>(); 
    
    //Constructor (parameter - local path to find the text file)
    public FloorReader(String path){ 
        try {
            readFile(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /* Method to store floor subsystem input file into an ArrayList of type FloorData
    *  IN: String that represents location of text file
    * OUT: None
    */
    private void readFile(String path) throws Exception {
        
        File file = new File(path);
        BufferedReader br = new BufferedReader(new FileReader(file)); 
        
        String str; 
        
        //format local time as Hours:Minutes:Seconds.NanoSeconds
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.S");
        
        //Read each line until null
        while ((str = br.readLine()) != null) {
            
          //parse chunk of information that is separated by a space
          String[] strSplit = str.split(" "); 
            
          //Converts String to type LocalTime to represent Time
          LocalTime localTime = LocalTime.parse(strSplit[0], formatter);
          
          //Converts String to type Int to represent floor number
          int floorNumber = Integer.parseInt(strSplit[1]);
          
          //Converts String to enum ButtonState (UP or DOWN)
          ButtonState buttonState = ButtonState.valueOf(strSplit[2]);
          
          //initializes new floorData and adds to ArrayList
          FloorData floorData = new FloorData(1, floorNumber, buttonState, localTime);
          floorRequests.add(floorData);
                              
        } 
    }
    
    //Getter to get stored ArrayList of FloorData (represents list of floors in text file)
    public ArrayList<FloorData> getFloorRequests() {
        return this.floorRequests;
    }

}
