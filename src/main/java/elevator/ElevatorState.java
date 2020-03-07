package elevator;

public enum ElevatorState {
    IDLE_DOOR_OPEN, IDLE_DOOR_CLOSED, MOVING_DOOR_CLOSED, MOVING_UP, MOVING_DOWN;

    public static final ElevatorState[] values = ElevatorState.values();
}
