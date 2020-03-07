package elevator;

import elevator.Elevator.Request;
import global.Globals;

public class ElevatorSubsystem implements Runnable {
    private final int elevatorId;
    private int currentHeight;
    private ElevatorState state;
    private ElevatorSystem elevatorSystem;
    private boolean initialState;

    public ElevatorSubsystem(final int elevatorId, final ElevatorSystem elevatorSystem) {
        this.elevatorId = elevatorId;
        this.currentHeight = 0;
        this.state = ElevatorState.IDLE_DOOR_OPEN;
        this.elevatorSystem = elevatorSystem;
        this.initialState = true;
    }

    public void run() {
        boolean waitingForDoorsToOpen = false;
        boolean sentReady = false;

        while (true) {
            switch (this.state) {
            // If MOVING_UP or MOVING_DOWN, move one floor per second.
            case MOVING_UP:
                currentHeight += Globals.FLOOR_HEIGHT / 10;
                Globals.sleep(100);
                break;
            case MOVING_DOWN:
                currentHeight -= Globals.FLOOR_HEIGHT / 10;
                Globals.sleep(100);
                break;
            // If IDLE_DOOR_OPEN, wait for two seconds to let people in/out then send the
            // Scheduler a READY request, signifying the Elevator is ready to move again.
            case IDLE_DOOR_OPEN:
                // We already sent a READY, no need to do it again.
                if (sentReady) {
                    Globals.sleep(5);
                    continue;
                }

                if (waitingForDoorsToOpen) {
                    waitingForDoorsToOpen = false;
                }

                if (initialState) {
                    // Running this loop is heavy, so we sleep for a tiny bit to give other threads
                    // a chance to run.
                    Globals.sleep(5);
                    break;
                }

                sentReady = true;

                Globals.sleep(2000);
                final byte[] sendData = new byte[3];
                sendData[0] = Globals.FROM_ELEVATOR;
                sendData[1] = (byte) this.elevatorId;
                sendData[2] = (byte) Request.READY.ordinal();
                this.elevatorSystem.sendData(sendData);

                break;
            case DOOR_CLOSED_FOR_IDLING:
                sentReady = false;

                // Don't send another request to open the doors.
                if (waitingForDoorsToOpen) {
                    Globals.sleep(5);
                    break;
                }

                final byte[] data = new byte[3];
                data[0] = Globals.FROM_ELEVATOR;
                data[1] = (byte) this.elevatorId;
                data[2] = (byte) Request.OPEN_DOORS.ordinal();
                this.elevatorSystem.sendData(data);
                waitingForDoorsToOpen = true;

                break;
            case DOOR_CLOSED_FOR_MOVING:
                break;
            }
        }
    }

    // TODO: Light up elevator lamps.
    // TODO: OPEN_DOORS and CLOSE_DOORS should take time.
    public ElevatorResponse updateState(final ElevatorAction elevatorAction) {
        initialState = false;

        switch (elevatorAction) {
        case MOVE_UP:
            this.state = ElevatorState.MOVING_UP;
            break;
        case MOVE_DOWN:
            this.state = ElevatorState.MOVING_DOWN;
            break;
        case STOP_MOVING:
            this.state = ElevatorState.DOOR_CLOSED_FOR_IDLING;
            break;
        case OPEN_DOORS:
            this.state = ElevatorState.IDLE_DOOR_OPEN;
            break;
        case CLOSE_DOORS:
            this.state = ElevatorState.DOOR_CLOSED_FOR_MOVING;
            break;
        }

        System.out.println("Elevator " + this.elevatorId + " state updated: " + this.state);

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
}
