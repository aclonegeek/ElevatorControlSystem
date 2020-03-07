package classes;

import elevator.ElevatorSystem;

public class RunnableElevatorSystem implements Runnable {
    final ElevatorSystem elevatorSystem;

    public RunnableElevatorSystem(final int numOfElevators) {
        this.elevatorSystem = new ElevatorSystem(numOfElevators);
    }

    public void run() {
        this.elevatorSystem.run();
    }

    public ElevatorSystem getElevatorSystem() {
        return this.elevatorSystem;
    }
}