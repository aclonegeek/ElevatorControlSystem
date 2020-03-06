package elevator;

import elevator.Elevator.Request;
import global.Globals;

public class ElevatorSubsystem implements Runnable {
    private final int elevatorId;
    private int currentHeight;
    private ElevatorState state;
    private ElevatorSystem elevatorSystem;

    public ElevatorSubsystem(final int elevatorId, final ElevatorSystem elevatorSystem) {
        this.elevatorId = elevatorId;
        this.currentHeight = 0;
        this.state = ElevatorState.MOVING_UP;
        this.elevatorSystem = elevatorSystem;
    }

    public void run() {
        while (true) {
            switch (this.state) {
            // If MOVING_UP or MOVING_DOWN, move one floor per second.
            case MOVING_UP:
                currentHeight += Globals.FLOOR_HEIGHT / 10;
                this.sleep(100);
                break;
            case MOVING_DOWN:
                currentHeight -= Globals.FLOOR_HEIGHT / 10;
                this.sleep(100);
                break;
            // If IDLE_DOOR_OPEN, wait for two seconds to let people in/out then send the
            // Scheduler a READY request, signifying the Elevator is ready to move again.
            case IDLE_DOOR_OPEN:
                this.sleep(2000);
                final byte[] sendData = new byte[3];
                sendData[0] = Globals.FROM_ELEVATOR;
                sendData[1] = (byte) this.elevatorId;
                sendData[2] = (byte) Request.READY.ordinal();
                this.elevatorSystem.sendData(sendData);
                break;
            default:
                break;
            }
        }
    }

    // TODO: Light up elevator lamps.
    // TODO: OPEN_DOORS and CLOSE_DOORS should take time.
    public ElevatorResponse updateState(final ElevatorAction elevatorAction) {
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

    public int getCurrentHeight() {
        return this.currentHeight;
    }

    public ElevatorState getState() {
        return this.state;
    }

    private void sleep(final int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            System.err.print(e);
        }
    }
}
