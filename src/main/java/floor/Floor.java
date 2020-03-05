package floor;

import java.util.ArrayList;
import floor.FloorData.ButtonState;

/**
 * Simulates the functionality of a particular floor.
 */
public class Floor {
    private ButtonState buttonState;
    private final int floor;
    private final FloorReader floorReader;

    private ArrayList<FloorData> requests;

    public Floor(final int floor) {
        this.buttonState = ButtonState.UNPRESSED;
        this.floor = floor;
        this.floorReader = new FloorReader();
        this.requests = new ArrayList<>();
    }

    public void addFloorRequest(final FloorData data) {
        this.requests.add(data);
    }

    public  void addFloorRequest(final String path) {
        this.requests = this.floorReader.readFile(this.getClass().getResource(path).getFile());
    }
}
