package elevator;

import global.Globals;

public class Elevator {
    private final ElevatorSubsystem elevatorSubsystem;
    private final ElevatorSystem elevatorSystem;

    public static enum Request {
        REGISTER, READY, OPEN_DOORS, STATE_CHANGED, INVALID;

        public static final Request[] values = Request.values();
    }

    public Elevator(final int elevatorId, final ElevatorSystem elevatorSystem) {
        this.elevatorSystem = elevatorSystem;
        this.elevatorSubsystem = new ElevatorSubsystem(elevatorId, elevatorSystem);

        // Create arrival sensors for each floor.
        for (int i = 0; i < Globals.MAX_FLOORS; i++) {
            new Thread(new ArrivalSensor(i, this.elevatorSubsystem)).start();
        }

        new Thread(this.elevatorSubsystem).start();
    }

    /*
     * Parse data and update ElevatorSystem state:
     * receiveData[0] signifies the data is from the Scheduler.
     * receiveData[1] is the id of the elevator.
     * receiveData[2] is the serialized ElevatorAction.
     *
     * Then return a response:
     * sendData[0] signifies the data is from an Elevator.
     * sendData[1] is the id of the Elevator.
     * sendData[3] is the serialized ElevatorState.
     * sendData[4] is the serialized ElevatorResponse.
     */
    public void processData(final byte[] receiveData) {
        final ElevatorAction action = ElevatorAction.values[receiveData[2]];
        final ElevatorState previousState = this.elevatorSubsystem.getState();
        final ElevatorResponse response = this.elevatorSubsystem.updateState(action);

        if (previousState != this.elevatorSubsystem.getState()) {
            final byte[] sendData = new byte[5];
            sendData[0] = Globals.FROM_ELEVATOR;
            sendData[1] = (byte) elevatorSubsystem.getElevatorId();
            sendData[2] = (byte) Request.STATE_CHANGED.ordinal();
            sendData[3] = (byte) this.elevatorSubsystem.getState().ordinal();
            sendData[4] = (byte) response.ordinal();

            this.elevatorSystem.sendData(sendData);
        }
    }

    public ElevatorSubsystem getSubsystem() {
        return this.elevatorSubsystem;
    }
}
