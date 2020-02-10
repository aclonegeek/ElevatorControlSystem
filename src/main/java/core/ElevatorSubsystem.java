package core;

import java.time.LocalTime;

public class ElevatorSubsystem {
    private enum MotorState {
        IDLE, MOVING_UP, MOVING_DOWN
    }

    private enum DoorState {
        OPEN, CLOSED
    }

    public enum ElevatorCommand {
        START_MOVING, STOP_MOVING, OPEN_DOORS, CLOSE_DOORS
    }

    private final int elevatorId;
    private int currentFloor;
    private int destinationFloor;
    private MotorState motorState;
    private DoorState doorState;

    public ElevatorSubsystem(final int elevatorId) {
        this.elevatorId = elevatorId;
        this.currentFloor = 0;
        this.destinationFloor = 0;
        this.motorState = MotorState.IDLE;
        this.doorState = DoorState.CLOSED;
    }

    // TODO: These actions need to take time
    public void updateState(ElevatorCommand elevatorCommand) {
        switch (elevatorCommand) {
        case START_MOVING:
            if (this.doorState == DoorState.OPEN) {
                this.doorState = DoorState.CLOSED;
            }

            if (this.destinationFloor > this.currentFloor) {
                this.motorState = MotorState.MOVING_UP;
            } else {
                this.motorState = MotorState.MOVING_DOWN;
            }
        default:
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
