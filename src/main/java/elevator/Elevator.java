package elevator;

public class Elevator implements Runnable {
    private final ElevatorSubsystem elevatorSubsystem;
    private final ElevatorSystem elevatorSystem;

    public Elevator(final int elevatorId, final ElevatorSystem elevatorSystem) {
        this.elevatorSystem = elevatorSystem;
        this.elevatorSubsystem = new ElevatorSubsystem(elevatorId);
    }

    @Override
    public void run() {
        // Do whatever it needs to do
        // ...
        
        this.elevatorSystem.sendData();
    }

    public ElevatorSubsystem getSubsystem() {
        return this.elevatorSubsystem;
    }
}
