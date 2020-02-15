package floor;

import java.util.ArrayList;

import scheduler.Scheduler;

/**
 * TODO: Update this - it does more than just a particular floor. Simulates the
 * functionality of a particular floor. Communicates with the scheduler through
 * FloorData objects.
 */
public class FloorSubsystem implements Runnable {
    private enum ButtonState {
        UP, DOWN, UNPRESSED
    }

    public static final int MAX_FLOORS = 50;

    private final Scheduler scheduler;
    private ButtonState buttonState;
    private final int floor;
    private final FloorReader floorReader;

    private ArrayList<FloorData> requests;

    public FloorSubsystem(final Scheduler scheduler, final int floor) {
        this.scheduler = scheduler;
        this.buttonState = ButtonState.UNPRESSED;
        this.floor = floor;
        this.floorReader = new FloorReader();
        this.requests = new ArrayList<>();
    }

    /**
     * Sends {@link FloorData} requests to the {@link Scheduler}.
     */
    @Override
    public void run() {
        while (true) {
            if (!this.requests.isEmpty()) {
                this.scheduler.scheduleElevator(this.requests.remove(0));
            }
        }
    }

    public void addFloorRequest(final FloorData data) {
        this.requests.add(data);
    }

    public void addFloorRequest(final String path) {
        this.requests = this.floorReader.readFile(this.getClass().getResource(path).getFile());
    }

    public void setButtonState(final ButtonState state) {
        this.buttonState = state;
    }

    public ButtonState getButtonState() {
        return this.buttonState;
    }

    public int getFloor() {
        return this.floor;
    }
}
