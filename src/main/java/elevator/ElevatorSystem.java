package elevator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import global.Ports;

// Contains all Elevators/ElevatorSubsystems and handles networking with Scheduler.
public class ElevatorSystem {
    private final ArrayList<Elevator> elevators;
    private DatagramSocket receiveSocket, sendSocket;
    private boolean sendingData;

    public ElevatorSystem(final int numOfElevators) {
        // Create Elevator threads.
        this.elevators = new ArrayList<>();
        for (int i = 0; i < numOfElevators; i++) {
            final Elevator elevator = new Elevator(i, this);
            this.elevators.add(elevator);
            new Thread(elevator).start();
        }

        // Create sockets to send and receive data through.
        try {
            this.receiveSocket = new DatagramSocket(Ports.ELEVATOR);
            this.sendSocket = new DatagramSocket(Ports.SCHEDULER_FOR_ELEVATOR);
        } catch (SocketException se) {
            System.err.println(se);
            System.exit(1);
        }

        this.sendingData = false;
    }

    // Register Elevators to Scheduler.
    private void registerElevators() {
        for (final Elevator elevator : this.elevators) {
            // Send packet to Scheduler to register Elevator.
            final byte[] sendData = new byte[2];
            sendData[0] = 2;
            sendData[1] = (byte) elevator.getSubsystem().getElevatorId();
            final DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length);

            try {
                this.sendSocket.send(sendPacket);
            } catch (IOException e) {
                System.err.println(e);
                System.exit(1);
            }

            // Block until Scheduler responds signifying the elevator has been registered.
            final DatagramPacket receivePacket = new DatagramPacket(new byte[0], 0);
            try {
                this.receiveSocket.receive(receivePacket);
            } catch (IOException e) {
                System.err.println(e);
                System.exit(1);
            }
        }
    }

    // Request and receive data from Scheduler.
    // Forward this event to the corresponding Elevator.
    private void receiveData() {
        // Send empty packet to request data.
        final DatagramPacket sendPacket = new DatagramPacket(new byte[0], 0);
        try {
            this.sendSocket.send(sendPacket);
        } catch (IOException e) {
            System.err.println(e);
            System.exit(1);
        }

        // Receive data back from Scheduler.
        final byte receiveData[] = new byte[4];
        final DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        try {
            this.receiveSocket.receive(receivePacket);
        } catch (IOException e) {
            System.err.println(e);
            System.exit(1);
        }

        // Remove trailing buffer 0s from receiveData
        final byte trimmedData[] = new byte[receivePacket.getLength()];
        System.arraycopy(receivePacket.getData(), receivePacket.getOffset(), trimmedData, 0,
                receivePacket.getLength());

        // Extract ElevatorAction from packet, forward it to the corresponding Elevator
        // receiveData[0] is the id of the elevator
        // receiveData[1] is the serialized ElevatorAction
        // receiveData[2+] is the destination floor
        final int elevatorId = trimmedData[0];
        final ElevatorAction elevatorAction = ElevatorAction.values[trimmedData[1]];

        String destinationFloorString = new String();
        for (int i = 2; i < trimmedData.length; i++) {
            destinationFloorString += trimmedData[i];
        }

        final int destinationFloor = Integer.parseInt(destinationFloorString);

        this.elevators.get(elevatorId).setDestinationFloor(destinationFloor);
        this.elevators.get(elevatorId).processAction(elevatorAction);
    }

    // Send data back to Scheduler.
    // This method will be called from inside an Elevator thread after processing
    // the data.
    // Synchronized so only one Elevator thread can interact with socket at a time.
    public synchronized void sendData(final int elevatorId, final ElevatorResponse response,
            final int currentFloor) {
        try {
            // If another thread is currently sending data, then wait until it is done
            while (this.sendingData) {
                this.wait();
            }

            this.sendingData = true;

            // Send data back to Scheduler.
            // If currentFloor is two digits, then byte array size is 5; otherwise, it is 4.
            final byte[] sendData = new byte[currentFloor > 9 ? 5 : 4];
            sendData[0] = 2;
            sendData[1] = (byte) elevatorId;
            sendData[2] = (byte) response.ordinal();

            if (currentFloor > 9) {
                String currentFloorString = Integer.toString(currentFloor);
                sendData[3] = (byte) Character.getNumericValue((currentFloorString.charAt(0)));
                sendData[4] = (byte) Character.getNumericValue((currentFloorString.charAt(1)));
            } else {
                sendData[3] = (byte) currentFloor;
            }

            final DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length);
            try {
                this.receiveSocket.send(sendPacket);
            } catch (IOException e) {
                System.err.println(e);
                System.exit(1);
            }

            // Block until receive response from Scheduler.
            final DatagramPacket receivePacket = new DatagramPacket(new byte[0], 0);
            try {
                this.receiveSocket.receive(receivePacket);
            } catch (IOException e) {
                System.err.println(e);
                System.exit(1);
            }

            this.sendingData = false;
            this.notifyAll();
        } catch (InterruptedException e) {
            System.err.println(e);
        }
    }

    public static void main(String args[]) {
        final ElevatorSystem elevatorSystem = new ElevatorSystem(3);
        elevatorSystem.registerElevators();

        while (true) {
            elevatorSystem.receiveData();
        }
    }
}
