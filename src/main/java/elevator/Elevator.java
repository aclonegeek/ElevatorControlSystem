package elevator;

import java.util.ArrayList;

import global.Globals;

public class Elevator {
    private final ElevatorSubsystem elevatorSubsystem;
    private final ElevatorSystem elevatorSystem;
    private ArrayList<ArrivalSensor> arrivalSensors;

    public static enum Request {
        REGISTER, READY, OPEN_DOORS, STATE_CHANGED, INVALID;

        public static final Request[] values = Request.values();
    }

    public Elevator(final int elevatorId, final ElevatorSystem elevatorSystem) {
        this.elevatorSystem = elevatorSystem;
        this.elevatorSubsystem = new ElevatorSubsystem(elevatorId, elevatorSystem);
        this.arrivalSensors = new ArrayList<>();

        // Create arrival sensors for each floor.
        for (int i = 0; i < Globals.MAX_FLOORS; i++) {
            final ArrivalSensor arrivalSensor = new ArrivalSensor(i, this.elevatorSubsystem);
            this.arrivalSensors.add(arrivalSensor);
            new Thread(arrivalSensor).start();
        }

        new Thread(this.elevatorSubsystem).start();
    }

    /*
     * Parse data and update ElevatorSystem state:
     * receiveData[0] signifies the data is from the Scheduler.
     * receiveData[1] is the id of the elevator.
     * receiveData[2] is the serialized ElevatorAction.
     * receiveData[3] is the destination floor (used to set the button state).
     *
     * Then return a response:
     * sendData[0] signifies the data is from an Elevator.
     * sendData[1] is the id of the Elevator.
     * sendData[2] is the serialized ElevatorResponse.
     * 
     * Return sendData for testing purposes.
     */
    public byte[] processData(final byte[] receiveData) {
        // Parse action and destination floor from receiveData.
        final ElevatorAction action = ElevatorAction.values[receiveData[2]];

        final ElevatorState previousState = this.elevatorSubsystem.getState();

        // Update ElevatorSubsystem state.
        final ElevatorResponse response = this.elevatorSubsystem.updateState(action);

        if (previousState != this.elevatorSubsystem.getState()) {
            final byte[] data = new byte[5];
            data[0] = Globals.FROM_ELEVATOR;
            data[1] = (byte) elevatorSubsystem.getElevatorId();
            data[2] = (byte) Request.STATE_CHANGED.ordinal();
            data[3] = (byte) this.elevatorSubsystem.getState().ordinal();
            data[4] = (byte) response.ordinal();

            this.elevatorSystem.sendData(data);
            return data;
        }
        
        return null;
    }

    public ElevatorSubsystem getSubsystem() {
        return this.elevatorSubsystem;
    }
    
    public ArrayList<ArrivalSensor> getArrivalSensors() {
        return this.arrivalSensors;
    }
}
