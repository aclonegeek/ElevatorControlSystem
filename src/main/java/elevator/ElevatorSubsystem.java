package elevator;

import java.time.LocalTime;

public class ElevatorSubsystem {
    private enum State {
        IDLE_DOOR_OPEN, IDLE_DOOR_CLOSED, MOVING_DOOR_CLOSED
    }

    private final int elevatorId;
    private int currentFloor;
    private int destinationFloor;
    private State state;

    public ElevatorSubsystem(final int elevatorId) {
        this.elevatorId = elevatorId;
        this.currentFloor = 0;
        this.destinationFloor = 0;
        this.state = State.IDLE_DOOR_CLOSED;
    }

    public void updateState(ElevatorAction elevatorAction) {
        switch (elevatorAction) {
        case START_MOVING:
            this.state = State.MOVING_DOOR_CLOSED;
        case STOP_MOVING:
            this.state = State.IDLE_DOOR_CLOSED;
        case OPEN_DOORS:
            this.state = State.IDLE_DOOR_OPEN;
        case CLOSE_DOORS:
            this.state = State.IDLE_DOOR_CLOSED;
        }
    }

    public void setCurrentFloor(final int currentFloor) {
        this.currentFloor = currentFloor;
    }

    public void setDestinationFloor(final int destinationFloor) {
        this.destinationFloor = destinationFloor;
    }

    public ElevatorData getElevatorData() {
        return new ElevatorData(this.elevatorId, this.currentFloor, this.destinationFloor, LocalTime.now());
    }

    public int getElevatorId() {
        return this.elevatorId;
    }
}
