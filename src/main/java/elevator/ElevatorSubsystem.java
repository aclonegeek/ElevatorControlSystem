package elevator;

import java.time.LocalTime;

import global.Globals;

public class ElevatorSubsystem implements Runnable {
    private final int elevatorId;
    private int currentHeight;
    private int currentFloor;
    private ElevatorState state;

    public ElevatorSubsystem(final int elevatorId) {
        this.elevatorId = elevatorId;
        this.currentFloor = 0;
        this.state = ElevatorState.MOVING_UP;
    }

    public void run() {
        while (true) {
            // For now, move up/down one floor per second.
            if (this.state == ElevatorState.MOVING_UP) {
                currentHeight += Globals.FLOOR_HEIGHT / 10;
            } else if (this.state == ElevatorState.MOVING_DOWN) {
                currentHeight -= Globals.FLOOR_HEIGHT / 10;
            }

            currentFloor = currentHeight / Globals.FLOOR_HEIGHT;

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {

            }
        }
    }

    // TODO: Set elevator lights based on selected destinationFloor.
    public ElevatorResponse updateState(final ElevatorAction elevatorAction, final int destinationFloor) {
        switch (elevatorAction) {
        case DESTINATION_REACHED:
            return ElevatorResponse.DESTINATION_REACHED;
        case MOVE_UP:
            this.state = ElevatorState.MOVING_UP;
            break;
        case MOVE_DOWN:
            this.state = ElevatorState.MOVING_DOWN;
            break;
        case STOP_MOVING:
            this.state = ElevatorState.IDLE_DOOR_CLOSED;
            break;
        case OPEN_DOORS:
            this.state = ElevatorState.IDLE_DOOR_OPEN;
            break;
        case CLOSE_DOORS:
            this.state = ElevatorState.IDLE_DOOR_CLOSED;
            break;
        }

        // For now, there are no errors.
        return ElevatorResponse.SUCCESS;
    }

    public int getElevatorId() {
        return this.elevatorId;
    }

    public int getCurrentHeight() {
        return this.currentHeight;
    }

    public int getCurrentFloor() {
        return this.currentFloor;
    }

    public ElevatorState getState() {
        return this.state;
    }

    public ElevatorData getElevatorData() {
        return new ElevatorData(this.elevatorId, this.currentFloor, LocalTime.now());
    }
}
