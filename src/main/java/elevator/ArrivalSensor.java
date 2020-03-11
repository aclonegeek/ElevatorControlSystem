package elevator;

import java.net.DatagramSocket;
import java.net.SocketException;

import global.Globals;

/**
 * Determines if an {@link Elevator} is within range.
 *
 * Continually checks to see if the {@link Elevator} is in range. If so, sends
 * data to the {@link Scheduler} to check whether it should stop at this floor.
 * If the {@link Elevator} is scheduled to stop here, the {@link Scheduler} will
 * notify the {@link ElevatorSystem}.
 *
 * sendData format is as follows:
 * sendData[0] signifies the data is from an {@link ArrivalSensor}.
 * sendData[1] is the id of the {@link Elevator}.
 * sendData[2] is the floor.
 */
public class ArrivalSensor implements Runnable {
    private final int floor;
    private final ElevatorSubsystem elevator;
    private DatagramSocket sendSocket;

    // Used for testing purposes. It denotes if an ArrivalSensor has detected an
    // elevator - if so, it stays true, even when the elevator moves out of
    // detection range.
    private boolean detectedElevator;

    public ArrivalSensor(final int floor, final ElevatorSubsystem elevator) {
        this.floor = floor;
        this.elevator = elevator;
        this.detectedElevator = false;

        try {
            this.sendSocket = new DatagramSocket();
        } catch (final SocketException se) {
            System.err.println(se);
            System.exit(1);
        }
    }

    @Override
    public void run() {
        while (true) {
            final int elevatorFloor = this.elevator.getCurrentHeight() / Globals.FLOOR_HEIGHT;
            final boolean elevatorMoving = elevator.getState() == ElevatorState.MOVING_UP
                    || elevator.getState() == ElevatorState.MOVING_DOWN;

            if (elevatorMoving && elevatorFloor == this.floor) {
                System.out.println("Floor " + floor + ": detected elevator " + elevator.getElevatorId());

                this.detectedElevator = true;

                // Send data to Scheduler. If the Elevator should stop at this floor, the
                // Scheduler will then notify the ElevatorSystem.
                final byte[] sendData = new byte[3];
                sendData[0] = Globals.FROM_ARRIVAL_SENSOR;
                sendData[1] = (byte) this.elevator.getElevatorId();
                sendData[2] = (byte) this.floor;
                
                // System.out.println("ArrivalSensor sending to port " + sendPacket.getPort() + ": " +
                // Arrays.toString(sendData));
                
                this.elevator.getElevatorSystem().sendData(sendData);

                // Sleep longer here so it doesn't keep sending data to Scheduler.
                Globals.sleep(1000);
            }

            Globals.sleep(50);
        }
    }

    /* METHODS USED FOR TESTING */
    public boolean hasDetectedElevator() {
        return this.detectedElevator;
    }

    public void closeSockets() {
        this.sendSocket.close();
    }
}
