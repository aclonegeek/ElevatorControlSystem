package core;

import java.io.Serializable;
import java.time.LocalTime;

/**
 * @summary Implementation of Floor data
 */

public class FloorData implements Serializable {
    private enum ButtonState {
        UP, DOWN, UNPRESSED
    }

    private static final long serialVersionUID = 1L;
    private final LocalTime time;
    private final int floorNumber;
    private final int elevatorId;
    private final ButtonState buttonState;

    public FloorData(final int elevatorId, final int floorNumber, final ButtonState buttonState,
            final LocalTime time) {
        this.elevatorId = elevatorId;
        this.floorNumber = floorNumber;
        this.time = time;
        this.buttonState = buttonState;
    }

    public LocalTime getTime() {
        return this.time;
    }

    public int getFloorNumber() {
        return this.floorNumber;
    }

    public int getElevatorId() {
        return this.elevatorId;
    }

    public ButtonState getButtonState() {
        return this.buttonState;
    }

}
