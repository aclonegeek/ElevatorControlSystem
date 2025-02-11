package elevator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;

import global.Globals;

/**
 * Contains all Elevators/ElevatorSubsystems and handles networking with {@link Scheduler}.
 */
public class ElevatorSystem {
    private final ArrayList<Elevator> elevators;
    private final HashMap<Integer, ElevatorFault> faults;
    private DatagramSocket receiveSocket, sendSocket;
    private boolean sendingData;

    public static void main(String args[]) {
         new ElevatorSystem(Globals.MAX_ELEVATORS).run();
    }

    public ElevatorSystem(final int numOfElevators) {
        this.faults = new HashMap<>();
        
        // Create Elevator threads.
        this.elevators = new ArrayList<>();
        for (int i = 1; i <= numOfElevators; i++) {
            final Elevator elevator = new Elevator(i, this);
            this.elevators.add(elevator);
        }

        // Create sockets to send and receive data.
        try {
            this.receiveSocket = new DatagramSocket(Globals.ELEVATOR_PORT);
            this.sendSocket = new DatagramSocket();
        } catch (final SocketException se) {
            System.err.println(se);
            System.exit(1);
        }

        this.sendingData = false;
    }

    public void run() {
        this.registerElevators();

        while (true) {
            this.receiveData();
        }
    }

    /**
     * Register {@link Elevator}s with {@link Scheduler}
     *
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

            // System.out.println("Sending to port " + sendPacket.getPort() + ": " + Arrays.toString(sendData));
            System.out.println("[elevator system] Elevator " + elevator.getSubsystem().getElevatorId() + ": sending registration request");

            this.sendData(sendData);
        }
    }

    /**
     * Receives data from Scheduler. Forward this event to the corresponding Elevator.
     */
    private void receiveData() {
        // Receive data back from Scheduler.
        final byte receiveData[] = new byte[4];
        final DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        try {
            this.receiveSocket.receive(receivePacket);
        } catch (final IOException e) {
            System.err.println(e);
            System.exit(1);
        }

        // System.out.println("Received: " + Arrays.toString(receiveData));

        switch (receiveData[0]) {
        case Globals.FROM_FLOOR:
            // receiveData[2] is floor number, [1] is the fault type.
            this.faults.put((int) receiveData[2], ElevatorFault.values()[receiveData[1]]);
            System.out.println("[elevator system] Received future fault: floor " + receiveData[2] + ", type " + ElevatorFault.values()[receiveData[1]]);
            break;
        case Globals.FROM_SCHEDULER:
            this.elevators.get(receiveData[1] - 1).processData(receiveData);
            break;
        }
    }

    /**
     * Sends data back to Scheduler.
     *
     * This method is called by an Elevator thread after processing the data.
     * Synchronized so only one Elevator thread can interact with socket at a time.
     */
    public synchronized void sendData(final byte[] sendData) {
        try {
            // If another thread is currently sending data, then wait until it is done
            while (this.sendingData) {
                this.wait();
            }

            this.sendingData = true;
            final DatagramPacket sendPacket =
                    new DatagramPacket(sendData, sendData.length, Globals.IP, Globals.SCHEDULER_PORT);

            // System.out.println("Sending to port " + sendPacket.getPort() + ": " + Arrays.toString(sendData) + "\n");
            try {
                this.sendSocket.send(sendPacket);
            } catch (final IOException e) {
                System.err.println(e);
                System.exit(1);
            }

            this.sendingData = false;
            this.notifyAll();
        } catch (final InterruptedException e) {
            System.err.println(e);
        }
    }
    
    public synchronized boolean hasFault(final int floorNumber, final ElevatorFault fault) {
        if (this.faults.get(floorNumber) == fault) {
            this.faults.remove(floorNumber);
            return true;
        }
        
        return false;
    }

    /* METHODS USED FOR TESTING */
    public ArrayList<Elevator> getElevators() {
        return this.elevators;
    }

    public void closeSockets() {
        this.receiveSocket.close();
        this.sendSocket.close();

        for (Elevator elevator : this.elevators) {
            for (ArrivalSensor arrivalSensor : elevator.getArrivalSensors()) {
                arrivalSensor.closeSockets();
            }
        }
    }
}
