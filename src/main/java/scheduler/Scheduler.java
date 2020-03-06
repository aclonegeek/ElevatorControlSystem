package scheduler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.HashMap;

import elevator.Elevator;
import elevator.ElevatorAction;
import floor.Floor;
import floor.FloorData;
import global.Globals;

/**
 * Coordinates the elevator and floor subsystems.
 */
public class Scheduler {
    public static enum SchedulerState {
        WAITING, SCHEDULING_ELEVATOR, WAITING_FOR_ELEVATOR_RESPONSE, HANDLING_ELEVATOR_RESPONSE,
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
            final DatagramPacket packet = this.receive();
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
        if (data.length == 0) {
            System.out.println("Received empty message.");
            return;
        }

        System.out.println("Received: " + Arrays.toString(data));

        switch (data[0]) {
        case Globals.FROM_FLOOR:
            this.handleFloorMessage(data, port);
            break;
        case Globals.FROM_ELEVATOR:
            this.handleElevatorMessage(data, port);
            break;
        case Globals.FROM_ARRIVAL_SENSOR:
            this.handleArrivalSensorMessage(data);
            break;
        default:
            System.err.println("Received invalid bytes.");
            break;
        }
    }

    private void handleFloorMessage(final byte[] data, final int port) {
        System.out.println("Handling a floor message.");

        switch (Floor.Request.values[data[2]]) {
        case REQUEST:
            this.closeElevatorDoors(data[1]);
            this.moveElevator(data[1], data[3]);
            break;
        case INVALID:
            break;
        default:
            System.out.println("Unknown floor message received.");
            break;
        }
    }

    private void handleElevatorMessage(final byte[] data, final int port) {
        System.out.println("Handling an elevator message.");

        switch (Elevator.Request.values[data[2]]) {
        case REGISTER:
            this.registerElevator(data[1]);
            // Reply with success.
            final byte reply[] = { 0 };
            final DatagramPacket packet =
                    new DatagramPacket(reply, reply.length, Globals.IP, Globals.ELEVATOR_PORT);
            this.send(packet);
            break;
        case READY:
            this.closeElevatorDoors(data[1]);
            // TODO: Maybe don't calculate the destination list again.
            this.moveElevator(data[1], this.elevatorStatuses.get(data[1]).getNextDestination());
            break;
        case UPDATE_LOCATION:
            break;
        case INVALID:
            break;
        default:
            System.out.println("Unknown elevator message received.");
            break;
        }
    }

    private void handleArrivalSensorMessage(final byte[] data) {
        System.out.println("Handling an arrival sensor message.");

        final int id = data[1];
        final int floor = data[2];

        this.elevatorStatuses.put(id, floor);

        if (!this.elevatorStatuses.get(id).destinations.contains(floor)) {
            return;
        }

        final byte reply[] = { Globals.FROM_SCHEDULER, data[1], (byte) ElevatorAction.STOP_MOVING.ordinal() };
        final DatagramPacket packet =
                new DatagramPacket(reply, reply.length, Globals.IP, Globals.ELEVATOR_PORT);

        this.send(packet);
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

    private void closeElevatorDoors(final int id) {
        final byte reply[] =
                { Globals.FROM_SCHEDULER, (byte) id, (byte) ElevatorAction.CLOSE_DOORS.ordinal() };
        final DatagramPacket packet =
                new DatagramPacket(reply, reply.length, Globals.IP, Globals.ELEVATOR_PORT);
        this.send(packet);
    }

    private void moveElevator(final int id, final int floor) {
        final BestElevator bestElevator = this.getBestElevator(id, floor);
        final ElevatorAction direction =
                bestElevator.direction == FloorData.ButtonState.UP ? ElevatorAction.MOVE_UP
                        : ElevatorAction.MOVE_DOWN;

        final byte reply[] = { Globals.FROM_SCHEDULER, bestElevator.id, (byte) direction.ordinal() };
        final DatagramPacket packet =
                new DatagramPacket(reply, reply.length, Globals.IP, Globals.ELEVATOR_PORT);
        this.send(packet);
    }

    /**
     * Registers an {@link Elevator} with the {@link Scheduler}.
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
