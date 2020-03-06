package elevator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;

import global.Globals;

// Contains all Elevators/ElevatorSubsystems and handles networking with Scheduler.
public class ElevatorSystem {
    private final ArrayList<Elevator> elevators;
    private DatagramSocket receiveSocket, sendSocket;
    private boolean sendingData;

    public static void main(String args[]) {
        final ElevatorSystem elevatorSystem = new ElevatorSystem(4);
        elevatorSystem.registerElevators();

        while (true) {
            elevatorSystem.receiveData();
        }
    }

    public ElevatorSystem(final int numOfElevators) {
        // Create Elevator threads.
        this.elevators = new ArrayList<>();
        for (int i = 1; i <= numOfElevators; i++) {
            final Elevator elevator = new Elevator(i, this);
            this.elevators.add(elevator);
        }

        // Create sockets to send and receive data through.
        try {
            this.receiveSocket = new DatagramSocket(Globals.ELEVATOR_PORT);
            this.sendSocket = new DatagramSocket();
        } catch (SocketException se) {
            System.err.println(se);
            System.exit(1);
        }

        this.sendingData = false;
    }

    /*
     * Register Elevators to Scheduler:
     * sendData[0] signifies the data is from an Elevator.
     * sendData[1] is the id of the elevator.
     * sendData[2] is the Request type.
     */
    private void registerElevators() {
        for (final Elevator elevator : this.elevators) {
            // Send packet to Scheduler to register Elevator.
            final byte[] sendData = new byte[3];
            sendData[0] = Globals.FROM_ELEVATOR;
            sendData[1] = (byte) elevator.getSubsystem().getElevatorId();
            sendData[2] = (byte) Elevator.Request.REGISTER.ordinal();
            final DatagramPacket sendPacket =
                    new DatagramPacket(sendData, sendData.length, Globals.IP, Globals.SCHEDULER_PORT);

            System.out.println("Sending to port " + sendPacket.getPort() + ": " + Arrays.toString(sendData));
            try {
                this.sendSocket.send(sendPacket);
            } catch (IOException e) {
                System.err.println(e);
                System.exit(1);
            }

            // Block until Scheduler responds signifying the elevator has been registered.
            // TODO: Handle success/failure cases.
//            final byte[] receiveData = new byte[1];
//            final DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
//            try {
//                this.receiveSocket.receive(receivePacket);
//            } catch (IOException e) {
//                System.err.println(e);
//                System.exit(1);
//            }
//
//            System.out.println("Received: " + Arrays.toString(receiveData) + "\n");
        }
    }

    // Receive data from Scheduler.
    // Forward this event to the corresponding Elevator.
    private void receiveData() {
        // Receive data back from Scheduler.
        final byte receiveData[] = new byte[4];
        final DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        try {
            this.receiveSocket.receive(receivePacket);
        } catch (IOException e) {
            System.err.println(e);
            System.exit(1);
        }

        // Remove trailing buffer 0s from receiveData.
        final byte trimmedData[] = new byte[receivePacket.getLength()];
        System.arraycopy(receivePacket.getData(), receivePacket.getOffset(), trimmedData, 0,
                receivePacket.getLength());

        // trimmedData[1] is an elevatorId. Forward data to the corresponding elevator.
        this.elevators.get(trimmedData[1]).processData(trimmedData);
    }

    // Send data back to Scheduler.
    // This method is called by an Elevator thread after processing the data.
    // Synchronized so only one Elevator thread can interact with socket at a time.
    public synchronized void sendData(byte[] sendData) {
        try {
            // If another thread is currently sending data, then wait until it is done
            while (this.sendingData) {
                this.wait();
            }

            this.sendingData = true;
            final DatagramPacket sendPacket =
                    new DatagramPacket(sendData, sendData.length, Globals.IP, Globals.SCHEDULER_PORT);

            System.out.println("Sending to port " + sendPacket.getPort() + ": " + Arrays.toString(sendData));
            try {
                this.sendSocket.send(sendPacket);
            } catch (IOException e) {
                System.err.println(e);
                System.exit(1);
            }

            // Block until receive response from Scheduler.
            // TODO: Handle success/failure cases.
//            final byte[] receiveData = new byte[1];
//            final DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
//            try {
//                this.receiveSocket.receive(receivePacket);
//            } catch (IOException e) {
//                System.err.println(e);
//                System.exit(1);
//            }
//
//            System.out.println("Received: " + Arrays.toString(receiveData) + "\n");

            this.sendingData = false;
            this.notifyAll();
        } catch (InterruptedException e) {
            System.err.println(e);
        }
    }
}
