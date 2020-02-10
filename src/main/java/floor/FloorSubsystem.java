package floor;

import java.util.ArrayList;

import scheduler.Scheduler;

/*
 * Floor subsystem which simulates the functionality of a particular floor.
 * Communicates with the scheduler through FloorData objects.
 */
public class FloorSubsystem implements Runnable {
    private enum ButtonState {
        UP, DOWN, UNPRESSED
    }

    private enum ElevatorState {
        UP, DOWN, IDLE, NONE
    }

    private final Scheduler scheduler;
    private ButtonState buttonState;
    private ElevatorState elevatorState;
    private final int floor;
    private final FloorReader floorReader;

    // Used for testing purposes.
    private int floorDataCount;

    public FloorSubsystem(final Scheduler scheduler, final int floor) {
        this.scheduler = scheduler;
        this.buttonState = ButtonState.UNPRESSED;
        this.elevatorState = ElevatorState.NONE;
        this.floor = floor;
        this.floorReader = new FloorReader();
        this.floorDataCount = 0;
    }

    /*
     * Reads input data from a file and continually tries to send FloorData events
     * to the scheduler.
     */
    @Override
    public void run() {
        final String filePath = this.getClass().getResource("/floorData.txt").getFile();
        ArrayList<FloorData> floorRequests = floorReader.readFile(filePath);
        while (true) {
            if (!floorRequests.isEmpty()) {
                this.scheduler.addFloorEvent(floorRequests.remove(0));
                FloorData returnedData = this.scheduler.removeElevatorEvent();
                System.out.println(
                        "Floor receives FloorData with floor number: " + returnedData.getFloorNumber());
                this.floorDataCount++;
            }
        }
    }

    public void setButtonState(final ButtonState state) {
        this.buttonState = state;
    }

    public ButtonState getButtonState() {
        return this.buttonState;
    }

    public void setElevatorState(final ElevatorState state) {
        this.elevatorState = state;
    }

    public ElevatorState getElevatorState() {
        return this.elevatorState;
    }

    public int getFloorNumber() {
        return this.floor;
    }

    public int getFloorDataCount() {
        return this.floorDataCount;
    }
}
