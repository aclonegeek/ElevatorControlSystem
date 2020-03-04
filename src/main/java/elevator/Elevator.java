package elevator;

import global.Globals;

public class Elevator implements Runnable {
    private final ElevatorSubsystem elevatorSubsystem;
    private final ElevatorSystem elevatorSystem;

    public Elevator(final int elevatorId, final ElevatorSystem elevatorSystem) {
        this.elevatorSystem = elevatorSystem;
        this.elevatorSubsystem = new ElevatorSubsystem(elevatorId);
    }

    @Override
    public void run() {
        // TODO: Need to put something here so it doesn't terminate.
    }

    /*
     * Parse data, update ElevatorSystem state, then return a response once it's complete.
     * receiveData[0] is the id of the elevator.
     * receiveData[1] is the serialized ElevatorAction.
     * receiveData[2+] is the destination floor.
     * TODO: Need to also send data back every time a new floor is reached.
     */
    public void processData(byte[] receiveData) {
        // Parse action and destination floor from receiveData.
        final ElevatorAction action = ElevatorAction.values[receiveData[1]];
        String destinationFloorString = new String();
        for (int i = 2; i < receiveData.length; i++) {
            destinationFloorString += receiveData[i];
        }
        final int destinationFloor = Integer.parseInt(destinationFloorString);
        
        // Update ElevatorSubsystem state.
        final ElevatorResponse response = this.elevatorSubsystem.updateState(action, destinationFloor);
        
        // Construct response message.
        final int currentFloor = elevatorSubsystem.getCurrentFloor();
        final byte[] sendData = new byte[currentFloor > 9 ? 5 : 4];
        sendData[0] = Globals.FROM_ELEVATOR;
        sendData[1] = (byte) elevatorSubsystem.getElevatorId();
        sendData[2] = (byte) response.ordinal();

        if (currentFloor > 9) {
            String currentFloorString = Integer.toString(currentFloor);
            sendData[3] = (byte) Character.getNumericValue((currentFloorString.charAt(0)));
            sendData[4] = (byte) Character.getNumericValue((currentFloorString.charAt(1)));
        } else {
            sendData[3] = (byte) currentFloor;
        }
        
        // Send response message.
        this.elevatorSystem.sendData(sendData);
    }

    public ElevatorSubsystem getSubsystem() {
        return this.elevatorSubsystem;
    }
}
