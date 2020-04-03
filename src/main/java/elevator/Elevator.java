package elevator;

import java.util.ArrayList;

import global.Globals;

public class Elevator {
    private final ElevatorSubsystem elevatorSubsystem;
    private final ElevatorSystem elevatorSystem;
    private ArrayList<ArrivalSensor> arrivalSensors;

    public static enum Request {
        REGISTER, READY, OPEN_DOORS, STATE_CHANGED;

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

    /**
     * Parses data and updates the {@link ElevatorSystem} state.
     *
     * @param receiveData the data sent from the {@link Scheduler}
     * receiveData[0] signifies the data is from the {@link Scheduler}.
     * receiveData[1] is the id of the elevator.
     * receiveData[2] is the serialized {@link ElevatorAction}.
     *
     * Then return a response:
     * sendData[0] signifies the data is from an {@link Elevator}.
     * sendData[1] is the id of the {@link Elevator}.
     * sendData[2] is the serialized Elevator.Request.
     * sendData[3] is the serialized {@link ElevatorState}.
     * sendData[4] is the serialized {@link ElevatorResponse}.
     *
     * @return the data sent back, or null if nothing sent back
     */
    public byte[] processData(final byte[] receiveData) {
        final ElevatorAction action = ElevatorAction.values[receiveData[2]];
        final ElevatorState previousState = this.elevatorSubsystem.getState();
        
        System.out.println("Elevator " + this.elevatorSubsystem.getElevatorId() + ": received action " + action);
        final ElevatorResponse response = this.elevatorSubsystem.updateState(action);

        if (previousState != this.elevatorSubsystem.getState()) {
            final byte[] sendData = new byte[5];
            sendData[0] = Globals.FROM_ELEVATOR;
            sendData[1] = (byte) elevatorSubsystem.getElevatorId();
            sendData[2] = (byte) Request.STATE_CHANGED.ordinal();
            sendData[3] = (byte) this.elevatorSubsystem.getState().ordinal();
            sendData[4] = (byte) response.ordinal();
            this.elevatorSystem.sendData(sendData);
            return sendData;
        }

        return null;
    }

    public ElevatorSubsystem getSubsystem() {
        return this.elevatorSubsystem;
    }

    /* METHODS USED FOR TESTING */
    public ArrayList<ArrivalSensor> getArrivalSensors() {
        return this.arrivalSensors;
    }
}
