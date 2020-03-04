package scheduler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.HashMap;

import elevator.Elevator;
import global.Globals;

/**
 * Coordinates the elevator and floor subsystems.
 */
public class Scheduler {
    public static enum SchedulerState {
        WAITING, SCHEDULING_ELEVATOR, WAITING_FOR_ELEVATOR_RESPONSE, HANDLING_ELEVATOR_RESPONSE,
    }

    // TODO: Grab these from Floor?
    private static enum FloorMessageType {
        REQUEST, INVALID,
    }

    private final HashMap<Integer, Integer> elevatorLocations;

    private SchedulerState state;

    private DatagramSocket receiveSocket;
    private DatagramSocket sendSocket;

    public static void main(String args[]) {
        new Scheduler().run();
    }

    public Scheduler() {
        this.elevatorLocations = new HashMap<>();
        this.state = SchedulerState.WAITING;

        try {
            this.receiveSocket = new DatagramSocket(Globals.SCHEDULER_PORT);
            this.sendSocket = new DatagramSocket();
        } catch (SocketException e) {
            System.err.println(e);
            System.exit(1);
        }
    }

    private void run() {
        System.out.println("Running scheduler...\n");

        while (true) {
            DatagramPacket packet = this.receive();
            this.handleMessage(packet.getData(), packet.getPort());
        }
    }

    private DatagramPacket receive() {
        byte data[] = new byte[10]; // TODO: Magic # (MAX_DATA?).
        DatagramPacket packet = new DatagramPacket(data, data.length);

        try {
            this.receiveSocket.receive(packet);
        } catch (IOException e) {
            System.err.println(e);
            System.exit(1);
        }

        return packet;
    }

    private void handleMessage(final byte[] data, final int port) {
        // Empty message.
        if (data.length == 0) {
            System.out.println("Received empty message.");
            return;
        }

        System.out.println("Received: " + Arrays.toString(data));

        switch (data[0]) {
        // Floor message.
        case Globals.FROM_FLOOR:
            this.handleFloorMessage(data, port);
            break;
        // Elevator message.
        case Globals.FROM_ELEVATOR:
            this.handleElevatorMessage(data, port);
            break;
        default:
            System.err.println("Received invalid bytes.");
            break;
        }
    }

    private void handleFloorMessage(final byte[] data, final int port) {
        System.out.println("Handling a floor message.");

        final int floor = data[1];

        switch (this.parseFloorMessage(data)) {
        case REQUEST:
            break;
        case INVALID:
            break;
        }
    }

    private void handleElevatorMessage(final byte[] data, final int port) {
        System.out.println("Handling an elevator message.");

        final int id = data[1];

        switch (this.parseElevatorMessage(data)) { // TODO: Don't need a method for this.
        case REGISTER:
            this.registerElevator(id);
            // Reply with success.
            byte reply[] = { 0 };
            DatagramPacket packet =
                    new DatagramPacket(reply, reply.length, Globals.IP, Globals.ELEVATOR_PORT);
            this.send(packet);
            break;
        case INVALID:
            break;
        }
    }

    private FloorMessageType parseFloorMessage(final byte[] data) {
        return FloorMessageType.REQUEST;
    }

    private Elevator.Request parseElevatorMessage(final byte[] data) {
        if (Elevator.Request.values()[data[2]] == Elevator.Request.REGISTER) {
            return Elevator.Request.REGISTER;
        }

        return Elevator.Request.INVALID;
    }

    // TODO: Move this to a utility class?
    private void send(final DatagramPacket packet) {
        System.out.println(
                "Sending to port " + packet.getPort() + ": " + Arrays.toString(packet.getData()) + "\n");

        try {
            this.sendSocket.send(packet);
        } catch (IOException e) {
            System.err.println(e);
            System.exit(1);
        }
    }

    /**
     * Registers an {@link Elevator} with the {@link Scheduler}, so that the
     * {@link Scheduler} can use it.
     *
     * @param id the {@link Elevator}'s id
     */
    public void registerElevator(final int id) {
        System.out.println("Registering elevator " + id + ".");
        this.elevatorLocations.put(id, 0); // All elevators start at the ground floor.
    }

    /**
     * Updates an {@link Elevator}'s location.
     *
     * @param elevatorId the ID of the {@link Elevator} to update
     * @param floor      the floor the {@link Elevator} is on
     */
    public void updateElevatorLocation(final int elevatorId, final int floor) {
        this.elevatorLocations.put(elevatorId, floor);
        this.notifyAll();
    }

    public SchedulerState getState() {
        return this.state;
    }
}
