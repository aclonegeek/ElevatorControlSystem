package elevator;

public enum ElevatorFault {
    // Hard faults (elevator shut down)
    ELEVATOR_STUCK,
    SENSOR_ERROR,
    
    // Recoverable faults
    DOOR_STUCK_OPEN,
    DOOR_STUCK_CLOSED,
}
