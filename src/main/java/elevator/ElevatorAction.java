package elevator;

public enum ElevatorAction {
    MOVE_UP, MOVE_DOWN, STOP_MOVING, OPEN_DOORS, CLOSE_DOORS, FUTURE_FAULT;

    // Array representation of the values which can be used to deserialize data.
    public static final ElevatorAction[] values = ElevatorAction.values();
}
