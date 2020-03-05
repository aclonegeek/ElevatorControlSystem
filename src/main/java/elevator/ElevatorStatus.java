package elevator;

import java.util.ArrayList;
import java.util.Collections;

import elevator.ElevatorState;

public class ElevatorStatus {

    private ElevatorState elevatorState;
    private ArrayList<Integer> destinations;
    private int currentFloor;

    public ElevatorStatus(ElevatorState elevatorState, int currentFloor) {
        this.destinations = new ArrayList<Integer>();
        this.elevatorState = elevatorState;
        this.currentFloor = currentFloor;
    }

    public void addDestination(int floor) {
        this.destinations.add(floor);
        //Sort the order of floors depending on which way the elevator is going
        if(this.elevatorState == ElevatorState.MOVING_UP) {
            Collections.sort(destinations);
        }
        else if(this.elevatorState == ElevatorState.MOVING_DOWN){
            Collections.sort(destinations, Collections.reverseOrder());
        }
    }
    
    //Since the order is sorted we remove first element
    public void removeDestination() {
        this.destinations.remove(0);
    }
    
    public int getCurrentFloor() {
        return this.currentFloor;
    }
    
    public void setCurrentFloor(int floor) {
        this.currentFloor = floor;
    }
    
    public ElevatorState getElevatorState() {
        return this.elevatorState;
    }
    
    public void setElevatorState(ElevatorState elevatorState) {
        this.elevatorState = elevatorState;
    }
    
    //return the number of stops the elevator will make before the floor which requested an elevator
    public int getStopsBefore(int floor) {
        int stops = 0;
        
        if(floor > this.currentFloor) {
            for(int destination : destinations) {
                if(destination < floor) {
                    stops++;
                }
            }
        } 
        else {
            for(int destination : destinations) {
                if(destination > floor) {
                    stops++;
                }
            }
        }
        
        return stops;
    }
}
