package elevator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;

import global.Globals;

/*
 * Continually checks to see if the Elevator is in range.
 * If so, sends data to the Scheduler to check whether it should stop at this floor.
 * If the Elevator is scheduled to stop here, the Scheduler will notify the ElevatorSystem.
 * 
 * sendData format is as follows:
 * sendData[0] signifies the data is from an ArrivalSensor.
 * sendData[1] is the id of the Elevator.
 * sendData[2] is the floor.
 */
public class ArrivalSensor implements Runnable {
    private final int floor;
    private final ElevatorSubsystem elevator;
    private DatagramSocket sendSocket;

    public ArrivalSensor(final int floor, final ElevatorSubsystem elevator) {
        this.floor = floor;
        this.elevator = elevator;

        try {
            this.sendSocket = new DatagramSocket();
        } catch (SocketException se) {
            System.err.println(se);
            System.exit(1);
        }
    }

    @Override
    public void run() {
        int lastDetectedFloor = 0;

        // TODO: Remove sysouts because they'll spam the ElevatorSystem console.
        while (true) {
            final int elevatorFloor = elevator.getCurrentHeight() / Globals.FLOOR_HEIGHT;
            if (elevatorFloor == floor && elevatorFloor != lastDetectedFloor) {
                System.out.println("Floor " + floor + " arrival sensor has detected elevator: "
                        + elevator.getElevatorId());
                lastDetectedFloor = floor;

                // Send data to Scheduler. If the Elevator should stop at this floor, the
                // Scheduler will then notify the ElevatorSystem.
                final byte[] sendData = new byte[3];
                sendData[0] = Globals.FROM_ARRIVAL_SENSOR;
                sendData[1] = (byte) elevator.getElevatorId();
                sendData[2] = (byte) floor;
                final DatagramPacket sendPacket =
                        new DatagramPacket(sendData, sendData.length, Globals.IP, Globals.SCHEDULER_PORT);

                System.out.println("ArrivalSensor sending to port " + sendPacket.getPort() + ": "
                        + Arrays.toString(sendData));
                try {
                    this.sendSocket.send(sendPacket);
                } catch (IOException e) {
                    System.err.println(e);
                    System.exit(1);
                }
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {

            }
        }
    }
}
