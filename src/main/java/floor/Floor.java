package floor;

import floor.FloorData.ButtonState;

/**
 * Simulates the functionality of a particular floor.
 */
public class Floor {
    private ButtonState buttonState;
    private final int floor;

    public Floor(final int floor) {
        this.buttonState = ButtonState.UNPRESSED;
        this.floor = floor;
    }
    
    public void setButtonState(final ButtonState buttonState) {
        this.buttonState = buttonState;
    }
    
    public ButtonState getButtonState() {
       return this.buttonState;
    }
    
    public int getFloor() {
        return this.floor;
    }
}