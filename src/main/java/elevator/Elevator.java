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
        // TODO: Not sure what run needs to do.
    }

    // Update elevator state then return the response and current floor.
    public void processAction(ElevatorAction action) {
        final ElevatorResponse response = this.elevatorSubsystem.updateState(action);
        this.elevatorSystem.sendData(this.elevatorSubsystem.getElevatorId(), response,
                this.elevatorSubsystem.getCurrentFloor());
    }

    public ElevatorSubsystem getSubsystem() {
        return this.elevatorSubsystem;
    }
}
