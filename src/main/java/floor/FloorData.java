package floor;

import java.io.Serializable;
import java.time.LocalTime;

public class FloorData implements Serializable {
    public static enum ButtonState {
        UP, DOWN, UNPRESSED
    }

    private static final long serialVersionUID = 1L;
    private final LocalTime time;
    private final int floor;
    private final ButtonState buttonState;

    public FloorData(final int floor, final ButtonState buttonState, final LocalTime time) {
        this.floor = floor;
        this.time = time;
        this.buttonState = buttonState;
    }

    public LocalTime getTime() {
        return this.time;
    }

    public int getFloor() {
        return this.floor;
    }

    public ButtonState getButtonState() {
        return this.buttonState;
    }
}
