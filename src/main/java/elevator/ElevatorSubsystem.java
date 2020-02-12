package elevator;

import java.time.LocalTime;

public class ElevatorSubsystem {
    private enum State {
        IDLE_DOOR_OPEN, IDLE_DOOR_CLOSED, MOVING_UP, MOVING_DOWN
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
        case MOVE_UP:
            this.state = State.MOVING_UP;
            currentFloor++;
        case MOVE_DOWN:
            this.state = State.MOVING_DOWN;
            currentFloor--;
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
    
    public int getElevatorId() {
        return this.elevatorId;
    }
    
    public int getCurrentFloor() {
        return this.currentFloor;
    }
    
    public ElevatorData getElevatorData() {
        return new ElevatorData(this.elevatorId, this.currentFloor, LocalTime.now());
    }
}
