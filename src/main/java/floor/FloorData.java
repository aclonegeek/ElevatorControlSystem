package floor;

import java.io.Serializable;
import java.time.LocalTime;

import elevator.ElevatorFault;

public class FloorData implements Serializable {
    public static enum ButtonState {
        UP, DOWN, UNPRESSED;

        public static final ButtonState[] values = ButtonState.values();
    }

    private static final long serialVersionUID = 1L;
    private final LocalTime time;
    private final int floor;
    private final int destination;
    private final ButtonState buttonState;
    private final ElevatorFault elevatorFault;
    private final Integer elevatorFaultFloor;
    

    public FloorData(final int floor, final ButtonState buttonState, final LocalTime time,
            final int destination, final ElevatorFault elevatorFault, final Integer elevatorFaultFloor) {
        this.floor = floor;
        this.time = time;
        this.buttonState = buttonState;
        this.destination = destination;
        this.elevatorFault = elevatorFault;
        this.elevatorFaultFloor = elevatorFaultFloor;
    }

    public LocalTime getTime() {
        return this.time;
    }

    public int getFloor() {
        return this.floor;
    }

    public int getDestination() {
        return this.destination;
    }

    public ButtonState getButtonState() {
        return this.buttonState;
    }
    
    public ElevatorFault getElevatorFault() {
        return this.elevatorFault;
    }
    
    public Integer getElevatorFaultFloor() {
        return this.elevatorFaultFloor;
    }
}
