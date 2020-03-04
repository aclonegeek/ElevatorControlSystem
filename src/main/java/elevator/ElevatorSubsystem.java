package elevator;

import java.time.LocalTime;

import global.Globals;

public class ElevatorSubsystem implements Runnable {
    private final int elevatorId;
    private final ElevatorSystem elevatorSystem;
    private int currentFloor;
    private ElevatorState state;

    public ElevatorSubsystem(final int elevatorId, final ElevatorSystem elevatorSystem) {
        this.elevatorId = elevatorId;
        this.elevatorSystem = elevatorSystem;
        this.currentFloor = 0;
        this.state = ElevatorState.IDLE_DOOR_OPEN;
    }

    public void run() {
        while (true) {
            if (this.state == ElevatorState.MOVING_UP) {
                currentFloor++;
                this.sendCurrentFloor();
            } else if (this.state == ElevatorState.MOVING_DOWN) {
                currentFloor--;
                this.sendCurrentFloor();
            }

            try {
                Thread.sleep(1000);
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

    public int getCurrentFloor() {
        return this.currentFloor;
    }

    public ElevatorState getState() {
        return this.state;
    }

    public ElevatorData getElevatorData() {
        return new ElevatorData(this.elevatorId, this.currentFloor, LocalTime.now());
    }

    /*
     * Send the elevator's updated currentFloor to the Scheduler:
     * sendData[0] signifies the data is from an Elevator.
     * sendData[1] is the id of the Elevator.
     * sendData[2] is the current floor.
     */
    public void sendCurrentFloor() {
        final byte[] sendData = new byte[3];
        sendData[0] = Globals.FROM_ELEVATOR;
        sendData[1] = (byte) this.elevatorId;
        sendData[2] = (byte) this.currentFloor;
        
        this.elevatorSystem.sendData(sendData);
    }
}
