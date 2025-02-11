package elevator;

import elevator.Elevator.Request;
import global.Globals;

public class ElevatorSubsystem implements Runnable {
    private final int elevatorId;
    private int currentHeight;
    private ElevatorState state;
    private final ElevatorSystem elevatorSystem;

    public ElevatorSubsystem(final int elevatorId, final ElevatorSystem elevatorSystem) {
        this.elevatorId = elevatorId;
        this.currentHeight = 0;
        this.state = ElevatorState.IDLE_DOOR_OPEN;
        this.elevatorSystem = elevatorSystem;
    }

    public void run() {
        boolean stuck = false;

        while (true) {
            int tempFloor = this.getCurrentFloor();
            switch (this.state) {
            // If MOVING_UP or MOVING_DOWN, move one floor per second.
            case MOVING_UP:
                stuck = false;
                currentHeight += Globals.FLOOR_HEIGHT / 10;
                Globals.sleep(100);
                break;
            case MOVING_DOWN:
                stuck = false;
                currentHeight -= Globals.FLOOR_HEIGHT / 10;
                Globals.sleep(100);
                break;
            // If IDLE_DOOR_OPEN, wait for two seconds to let people in/out then send the
            // Scheduler a READY request, signifying the Elevator is ready to move again.
            case IDLE_DOOR_OPEN:
                Globals.sleep(2000);
                
                if (this.state != ElevatorState.IDLE_DOOR_OPEN) {
                    break;
                }
                
                final byte[] sendData = new byte[3];
                sendData[0] = Globals.FROM_ELEVATOR;
                sendData[1] = (byte) this.elevatorId;
                sendData[2] = (byte) Request.READY.ordinal();
                this.elevatorSystem.sendData(sendData);
                this.state = ElevatorState.WAITING;
                break;
            case DOOR_CLOSED_FOR_IDLING:
                if (stuck) break;
                final byte[] data = new byte[3];
                data[0] = Globals.FROM_ELEVATOR;
                data[1] = (byte) this.elevatorId;
                data[2] = (byte) Request.OPEN_DOORS.ordinal();
                this.elevatorSystem.sendData(data);
                this.state = ElevatorState.WAITING;
                break;
            case DOOR_CLOSED_FOR_MOVING:
                break;
            case WAITING:
                Globals.sleep(5);
            }

            // Check if fault should occur here (ie. elevator gets stuck).
            final int currentFloor = this.getCurrentFloor();
            if (currentFloor != tempFloor && this.elevatorSystem.hasFault(currentFloor, ElevatorFault.ELEVATOR_STUCK)) {
                System.out.println("[elevator system] FAULT: Elevator " + elevatorId + " is stuck!");
                this.state = ElevatorState.DOOR_CLOSED_FOR_IDLING;
                stuck = true;
            }
        }
    }

    public ElevatorResponse updateState(final ElevatorAction elevatorAction) {
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
            if (this.elevatorSystem.hasFault(this.getCurrentFloor(), ElevatorFault.DOOR_STUCK_CLOSED)) {
                System.out.println("[elevator system] FAULT: Elevator " + elevatorId + "'s door is stuck closed!");
                return ElevatorResponse.FAILURE;
            }
            
            this.state = ElevatorState.IDLE_DOOR_OPEN;
            break;
        case CLOSE_DOORS:
            if (this.elevatorSystem.hasFault(this.getCurrentFloor(), ElevatorFault.DOOR_STUCK_OPEN)) {
                System.out.println("[elevator system] FAULT: Elevator " + elevatorId + "'s door is stuck open!");
                return ElevatorResponse.FAILURE;
            }
            
            this.state = ElevatorState.DOOR_CLOSED_FOR_MOVING;
            break;
        default:
            break;
        }

        System.out.println("[elevator system] Elevator " + this.elevatorId + ": state changed to " + this.state);
        if (this.state == ElevatorState.DOOR_CLOSED_FOR_IDLING) {
            System.out.println("[elevator system] Elevator " + this.elevatorId + ": arrived at floor " + this.getCurrentFloor());
        }

        // For now, there are no errors.
        return ElevatorResponse.SUCCESS;
    }

    public void setCurrentHeight(final int height) {
        this.currentHeight = height;
    }

    public int getElevatorId() {
        return this.elevatorId;
    }

    public int getCurrentHeight() {
        return this.currentHeight;
    }

    public int getCurrentFloor() {
        return this.currentHeight / Globals.FLOOR_HEIGHT;
    }

    public ElevatorState getState() {
        return this.state;
    }

    public ElevatorSystem getElevatorSystem() {
        return this.elevatorSystem;
    }
}
