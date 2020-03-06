package scheduler;

import java.util.ArrayList;
import java.util.Collections;

import elevator.ElevatorState;

public class ElevatorStatus {

    private ElevatorState state;
    private ArrayList<Integer> destinations;
    private int currentFloor;

    public ElevatorStatus(final ElevatorState state,final int currentFloor) {
        this.destinations = new ArrayList<Integer>();
        this.state = state;
        this.currentFloor = currentFloor;
    }

    public void addDestination(final int floor) {
        this.destinations.add(floor);
    }
    
    public ArrayList<Integer> getDestinations(){
        return this.destinations;
    }
    
    public void removeDestination() {
        this.destinations.remove(0);
    }
    
    public int getCurrentFloor() {
        return this.currentFloor;
    }
    
    public void setCurrentFloor(final int floor) {
        this.currentFloor = floor;
    }
    
    public ElevatorState getState() {
        return this.state;
    }
    
    public void setState(final ElevatorState state) {
        this.state = state;
    }
}
