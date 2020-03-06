package elevator;

import global.Globals;

/*
 * Continually checks to see if the Elevator is in range.
 * If so, sends data to the Scheduler to check whether it should stop at this floor.
 * If the Elevator is scheduled to stop here, the Scheduler will notify the ElevatorSystem.
 */
public class ArrivalSensor implements Runnable {
    private final int floor;
    private final ElevatorSubsystem elevator;
    int lastDetectedFloor;

    public ArrivalSensor(final int floor, final ElevatorSubsystem elevator) {
        this.floor = floor;
        this.elevator = elevator;
        this.lastDetectedFloor = 0;
    }

    @Override
    public void run() {
        while (true) {
            final int elevatorFloor = elevator.getCurrentHeight() / Globals.FLOOR_HEIGHT;
            if (elevatorFloor == floor && elevatorFloor != lastDetectedFloor) {
                System.out.println("Floor " + floor + " has detected elevator " + elevator.getElevatorId());
                this.lastDetectedFloor = floor;
                
                // TODO: Send data to Scheduler to see if the Elevator should stop here.
            }
            
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {

            }
        }
    }
}
