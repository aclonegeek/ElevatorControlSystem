package elevator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;

import global.Ports;

// Contains all Elevators/ElevatorSubsystems and handles networking with Scheduler.
public class ElevatorSystem {
    private ArrayList<Thread> elevators;
    private DatagramPacket receivePacket, sendPacket;
    private DatagramSocket receiveSocket, sendSocket;

    public ElevatorSystem(final int numOfElevators) {
        // Create Elevator threads.
        elevators = new ArrayList<>();
        for (int i = 0; i < numOfElevators; i++) {
            final Elevator elevator = new Elevator(i, this);
            final Thread elevatorThread = new Thread(elevator);
            elevators.add(elevatorThread);
            elevatorThread.start();
        }

        // Create socket to send and receive data through.
        try {
            receiveSocket = new DatagramSocket(Ports.ELEVATOR);
            sendSocket = new DatagramSocket(Ports.SCHEDULER);
        } catch (SocketException se) {
            System.err.println(se);
            System.exit(1);
        }
    }

    // TODO: Register elevators to Scheduler.
    private void registerElevators() {

    }

    // Request and receive data from Scheduler.
    // Forward this event to the corresponding Elevator.
    private void receiveData() {
        // Send empty packet to request data.
        sendPacket = new DatagramPacket(new byte[0], 0);
        try {
            sendSocket.send(sendPacket);
        } catch (IOException e) {
            System.err.println(e);
            System.exit(1);
        }

        // Receive data back from Scheduler.
        byte receiveData[] = new byte[1000];
        receivePacket = new DatagramPacket(receiveData, receiveData.length);
        try {
            receiveSocket.receive(receivePacket);
        } catch (IOException e) {
            System.err.println(e);
            System.exit(1);
        }

        // TODO: Convert datagram packet into an ElevatorAction.
        // TODO: Will probably need to receive the destination floor as well.

    }

    // Send data back to Scheduler.
    // This method will be called from inside an Elevator thread.
    // Synchronized so only one Elevator thread can interact with socket at a time.
    public synchronized void sendData(ElevatorResponse response, int currentFloor) {

    }

    public static void main(String args[]) {
        ElevatorSystem elevatorSystem = new ElevatorSystem(Config.NUM_OF_ELEVATORS);
        elevatorSystem.registerElevators();

        while (true) {
            elevatorSystem.receiveData();
        }
    }
}
