package floor;

import java.io.Serializable;
import java.time.LocalTime;

/**
 * Implements a basic class which holds floor data. Only has a constructor,
 * getters, and setters. FloorData instances are passed between the subsystems
 * to signify events.
 */
public class FloorData implements Serializable {
    public static enum ButtonState {
        UP, DOWN, UNPRESSED
    }

    private static final long serialVersionUID = 1L;
    private final LocalTime time;
    private final int floorNumber;
    private final int elevatorId;
    private final ButtonState buttonState;

    public FloorData(final int elevatorId, final int floorNumber, final ButtonState up,
            final LocalTime time) {
        this.elevatorId = elevatorId;
        this.floorNumber = floorNumber;
        this.time = time;
        this.buttonState = up;
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
