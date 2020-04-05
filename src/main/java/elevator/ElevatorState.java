package elevator;

public enum ElevatorState {
    IDLE_DOOR_OPEN, DOOR_CLOSED_FOR_IDLING, DOOR_CLOSED_FOR_MOVING, MOVING_UP, MOVING_DOWN, WAITING;

    public static final ElevatorState[] values = ElevatorState.values();
}
