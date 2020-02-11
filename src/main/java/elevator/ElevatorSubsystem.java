package elevator;

import java.time.LocalTime;

public class ElevatorSubsystem {
    private enum State {
        IDLE_DOOR_OPEN, IDLE_DOOR_CLOSED, MOVING_DOOR_CLOSED
    }

    private final int elevatorId;
    private int currentFloor;
    private State state;

    public ElevatorSubsystem(final int elevatorId) {
        this.elevatorId = elevatorId;
        this.currentFloor = 0;
        this.state = State.IDLE_DOOR_CLOSED;
    }

    public ElevatorResponse updateState(ElevatorAction elevatorAction) {
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
        
        // For now, assume all state changes are valid
        return ElevatorResponse.SUCCESS;
    }

    public void setCurrentFloor(final int currentFloor) {
        this.currentFloor = currentFloor;
    }

    public ElevatorData getElevatorData() {
        return new ElevatorData(this.elevatorId, this.currentFloor, LocalTime.now());
    }

    public int getElevatorId() {
        return this.elevatorId;
    }
}
