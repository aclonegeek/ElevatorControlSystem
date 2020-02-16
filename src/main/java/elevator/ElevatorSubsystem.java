package elevator;

import java.time.LocalTime;

public class ElevatorSubsystem {
    private final int elevatorId;
    private int currentFloor;
    private ElevatorState state;

    public ElevatorSubsystem(final int elevatorId) {
        this.elevatorId = elevatorId;
        this.currentFloor = 0;
        this.state = ElevatorState.IDLE_DOOR_OPEN;
    }

    public ElevatorResponse updateState(final ElevatorAction elevatorAction) {
        switch (elevatorAction) {
        case DESTINATION_REACHED:
            return ElevatorResponse.DESTINATION_REACHED;
        case MOVE_UP:
            this.state = ElevatorState.MOVING_UP;
            currentFloor++;
            break;
        case MOVE_DOWN:
            this.state = ElevatorState.MOVING_DOWN;
            currentFloor--;
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
