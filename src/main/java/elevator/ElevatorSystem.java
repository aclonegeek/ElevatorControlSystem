package elevator;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;

// Contains all Elevators/ElevatorSubsystems and handles networking with Scheduler.
public class ElevatorSystem {
    private ArrayList<Thread> elevators;
    private DatagramPacket receivePacket, sendPacket;
    private DatagramSocket socket;
    
    public ElevatorSystem(final int numOfElevators) {
        elevators = new ArrayList<>();
        
        // Create Elevator threads
        for (int i = 0; i < numOfElevators; i++) {
            final Elevator elevator = new Elevator(i, this);
            final Thread elevatorThread = new Thread(elevator);
            elevators.add(elevatorThread);
            elevatorThread.start();
        }
        
        // Create socket to send and receive data through.
        try {
            socket = new DatagramSocket();
        } catch (SocketException se) {
            System.err.println(se);
            System.exit(1);
        }
    }
    
    // Register elevators to Scheduler.
    private void registerElevators() {
        
    }
    
    // Request and receive data from Scheduler.
    // Forward this event to the corresponding Elevator.
    private void receiveData() {
        
    }
    
    // Send data back to Scheduler.
    // This method will be called from inside an Elevator thread.
    // Synchronized so only one Elevator thread can interact with the socket at a time.
    public synchronized void sendData() {
        
    }
    
    public static void main(String args[]) {
        ElevatorSystem elevatorSystem = new ElevatorSystem(3);
        elevatorSystem.registerElevators();
        
        while (true) {
            elevatorSystem.receiveData();
        }
    }
}
