package elevator;

import global.Globals;

public class Elevator {
    private final ElevatorSubsystem elevatorSubsystem;
    private final ElevatorSystem elevatorSystem;

    public static enum Request {
        REGISTER, UPDATE_LOCATION, INVALID;

        public static final Request[] values = Request.values();
    }

    public Elevator(final int elevatorId, final ElevatorSystem elevatorSystem) {
        this.elevatorSystem = elevatorSystem;
        this.elevatorSubsystem = new ElevatorSubsystem(elevatorId, elevatorSystem);
        new Thread(elevatorSubsystem).start();
    }

    /*
     * Parse data and update ElevatorSystem state:
     * receiveData[0] is the id of the Elevator.
     * receiveData[1] is the serialized ElevatorAction.
     * receiveData[2] is the current floor.
     * 
     * Then return a response:
     * sendData[0] signifies the data is from an Elevator.
     * sendData[1] is the id of the Elevator.
     * sendData[2] is the serialized ElevatorResponse.
     * sendData[3] is the current floor.
     */
    public void processData(byte[] receiveData) {
        // Parse action and destination floor from receiveData.
        final ElevatorAction action = ElevatorAction.values[receiveData[1]];
        final int destinationFloor = (int) receiveData[2];

        // Update ElevatorSubsystem state.
        final ElevatorResponse response = this.elevatorSubsystem.updateState(action, destinationFloor);

        // Construct response message.
        final int currentFloor = elevatorSubsystem.getCurrentFloor();
        final byte[] sendData = new byte[4];
        sendData[0] = Globals.FROM_ELEVATOR;
        sendData[1] = (byte) elevatorSubsystem.getElevatorId();
        sendData[2] = (byte) response.ordinal();
        sendData[3] = (byte) currentFloor;

        // Send response message.
        this.elevatorSystem.sendData(sendData);
    }

    public ElevatorSubsystem getSubsystem() {
        return this.elevatorSubsystem;
    }
}
