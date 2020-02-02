package core;

import java.util.ArrayList;

/**
 * @summary Floor subsystem simulates the functionality of a particular floor
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

    public FloorSubsystem(final Scheduler scheduler, final int floor) {
        this.scheduler = scheduler;
        this.buttonState = ButtonState.UNPRESSED;
        this.elevatorState = ElevatorState.NONE;
        this.floor = floor;
        this.floorReader = new FloorReader();
    }

    @Override
    public void run() {
        this.scheduler.registerFloorSubsystem(this);
        final String filePath = this.getClass().getResource("/floorData.txt").getFile();
        ArrayList<FloorData> floorRequests = floorReader.readFile(filePath);
        System.out.println("floorRequests size: " + floorRequests.size());
        while (true) {
            if (!floorRequests.isEmpty()) {
                this.scheduler.addFloorEvent(floorRequests.remove(0));
                FloorData returnedData = this.scheduler.getElevatorEvent();
                System.out.println("Floor receives FloorData: " + returnedData.getFloorNumber());
                System.out.println("floorRequests size: " + floorRequests.size());
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
}
