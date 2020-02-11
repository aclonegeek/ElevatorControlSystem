package floor;

import java.util.ArrayList;

import scheduler.Scheduler;

/**
 * TODO: Update this - it does more than just a particular floor.
 * Simulates the functionality of a particular floor.
 * Communicates with the scheduler through FloorData objects.
 */
public class FloorSubsystem implements Runnable {
    private enum ButtonState {
        UP, DOWN, UNPRESSED
    }

    public static int MAX_FLOORS = 50;

    private final Scheduler scheduler;
    private ButtonState buttonState;
    private final int floor;
    private final FloorReader floorReader;

    public FloorSubsystem(final Scheduler scheduler, final int floor) {
        this.scheduler = scheduler;
        this.buttonState = ButtonState.UNPRESSED;
        this.floor = floor;
        this.floorReader = new FloorReader();
    }

    /**
     * Reads input data from a file and continually tries to send {@link FloorData} events
     * to the {@link Scheduler}.
     */
    @Override
    public void run() {
        final String filePath = this.getClass().getResource("/floorData.txt").getFile();
        final ArrayList<FloorData> floorRequests = floorReader.readFile(filePath);

        while (true) {
            if (!floorRequests.isEmpty()) {
                this.scheduler.addElevatorEvent(floorRequests.remove(0));
            }
        }
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
