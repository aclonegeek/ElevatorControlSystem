package floor;

import java.util.ArrayList;
import floor.FloorData.ButtonState;
import scheduler.Scheduler;

/**
 * Simulates the functionality of a particular floor.
 */
public class Floor implements Runnable {

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
            this.handleRequests();
        }
    }

    private synchronized void handleRequests() {
        while (this.requests.isEmpty()) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                System.err.println(e);
            }
        }

        this.scheduler.scheduleElevator(this.requests.remove(0));
    }

    public synchronized void addFloorRequest(final FloorData data) {
        this.requests.add(data);
        this.notifyAll();
    }

    public synchronized void addFloorRequest(final String path) {
        this.requests = this.floorReader.readFile(this.getClass().getResource(path).getFile());
        this.notifyAll();
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