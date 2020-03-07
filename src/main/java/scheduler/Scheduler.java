package scheduler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

import elevator.Elevator;
import elevator.ElevatorAction;
import elevator.ElevatorState;
import floor.Floor;
import floor.FloorData.ButtonState;
import global.Globals;

/**
 * Coordinates the elevator and floor subsystems.
 */
public class Scheduler {
    public static enum SchedulerState {
        WAITING, HANDLING_MESSAGE,
    }

    private final HashMap<Integer, ElevatorStatus> elevatorStatuses;

    private SchedulerState state;

    private DatagramSocket receiveSocket;
    private DatagramSocket sendSocket;

    public static void main(String args[]) {
        new Scheduler().run();
    }

    public Scheduler() {
        this.elevatorStatuses = new HashMap<>();
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

        this.state = SchedulerState.HANDLING_MESSAGE;

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

        this.state = SchedulerState.WAITING;
    }

    private void handleFloorMessage(final byte[] data, final int port) {
        System.out.println("Handling a floor message.");

        switch (Floor.Request.values[data[2]]) {
        case REQUEST:
            final ElevatorState direction = this.checkDirection(ButtonState.values[data[4]]);
            final BestElevator bestElevator = this.getBestElevator(data[1], direction);
            this.elevatorStatuses.get(bestElevator.id).addDestination(data[3]);
            this.moveElevator(bestElevator.id, bestElevator.direction);
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
            // final byte reply[] = { 0 };
            // final DatagramPacket packet =
            //         new DatagramPacket(reply, reply.length, Globals.IP, Globals.ELEVATOR_PORT);
            // this.send(packet);
            System.out.println();
            break;
        case READY:
            final int id = data[1];

            if (this.elevatorStatuses.get(id).getDestinations().isEmpty()) {
                break;
            }

            this.closeElevatorDoors(id);

            // TODO: Tidy this up.
            final int currentFloor = this.elevatorStatuses.get(id).getCurrentFloor();
            final int destination = this.elevatorStatuses.get(id).getDestinations().get(0);
            final int distance = currentFloor - destination;

            final ElevatorAction direction = distance < 0 ? ElevatorAction.MOVE_UP : ElevatorAction.MOVE_DOWN;

            this.moveElevator(id, direction);
            break;
        case OPEN_DOORS:
            this.openElevatorDoors(data[1]);
            break;
        case STATE_CHANGED:
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

        this.elevatorStatuses.get(id).setCurrentFloor(floor);

        if (!this.elevatorStatuses.get(id).getDestinations().contains(floor)) {
            return;
        }

        final byte reply[] = { Globals.FROM_SCHEDULER, data[1], (byte) ElevatorAction.STOP_MOVING.ordinal() };
        final DatagramPacket packet =
                new DatagramPacket(reply, reply.length, Globals.IP, Globals.ELEVATOR_PORT);

        this.send(packet);
        
        this.elevatorStatuses.get(id).getDestinations().remove(0);
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

    private void openElevatorDoors(final int id) {
        final byte reply[] =
                { Globals.FROM_SCHEDULER, (byte) id, (byte) ElevatorAction.OPEN_DOORS.ordinal() };
        final DatagramPacket packet =
                new DatagramPacket(reply, reply.length, Globals.IP, Globals.ELEVATOR_PORT);
        this.send(packet);
    }

    private void closeElevatorDoors(final int id) {
        final byte reply[] =
                { Globals.FROM_SCHEDULER, (byte) id, (byte) ElevatorAction.CLOSE_DOORS.ordinal() };
        final DatagramPacket packet =
                new DatagramPacket(reply, reply.length, Globals.IP, Globals.ELEVATOR_PORT);
        this.send(packet);
    }

    private void moveElevator(final int id, final ElevatorAction direction) {
        final byte reply[] = { Globals.FROM_SCHEDULER, (byte) id, (byte) direction.ordinal() };
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
        // All elevators start at the ground floor.
        this.elevatorStatuses.put(id, new ElevatorStatus(ElevatorState.IDLE_DOOR_OPEN, 0));
    }

    /**
     * Returns the closest elevator to the specified floor.
     *
     * @return the ID and number of floors to travel for the {@link Elevator}
     *         closest to the specified floor.
     */
    public ElevatorState checkDirection(ButtonState state) {
        if (state == ButtonState.UP) {
            return ElevatorState.MOVING_UP;
        } else if (state == ButtonState.DOWN) {
            return ElevatorState.MOVING_DOWN;
        } else {
            return ElevatorState.IDLE_DOOR_CLOSED;
        }
    }

    // TODO: Change ElevatorState to ElevatorAction.
    public BestElevator getBestElevator(final int floor, final ElevatorState state) {
        int bestElevatorID = -1;
        int tempElevatorID;
        ElevatorStatus tempElevatorStatus;
        boolean anotherElevatorOnRoute = false;

        for (final Entry<Integer, ElevatorStatus> entry : this.elevatorStatuses.entrySet()) {
            tempElevatorID = entry.getKey();
            tempElevatorStatus = entry.getValue();

            // Init the bestId to the first ID.
            if (bestElevatorID == -1) {
                bestElevatorID = tempElevatorID;
            }

            int tempStopsBetween = getStopsBetween(tempElevatorStatus, floor);
            int bestStopsBetween = getStopsBetween(this.elevatorStatuses.get(bestElevatorID), floor);

            // Best elevator is idle.
            if (tempElevatorStatus.getState() == ElevatorState.IDLE_DOOR_CLOSED ||
                    tempElevatorStatus.getState() == ElevatorState.IDLE_DOOR_OPEN) {
                bestElevatorID = tempElevatorID;
                break;
            }

            // Best elevator is one moving with the floor on its path going upwards.
            if (tempElevatorStatus.getState() == state && state == ElevatorState.MOVING_UP) {
                if (tempElevatorStatus.getCurrentFloor() < floor) {
                    if (tempStopsBetween <= bestStopsBetween) {
                        bestElevatorID = tempElevatorID;
                        anotherElevatorOnRoute = true;
                    }
                }
            }

            // Best elevator is one moving with the floor on its path going downwards.
            else if (tempElevatorStatus.getState() == state && state == ElevatorState.MOVING_DOWN) {
                if (tempElevatorStatus.getCurrentFloor() > floor) {
                    if (tempStopsBetween <= bestStopsBetween) {
                        bestElevatorID = tempElevatorID;
                        anotherElevatorOnRoute = true;
                    }
                }
            }

            // Best elevator is one the one with least stops between.
            if (tempElevatorStatus.getDestinations().size() <= this.elevatorStatuses.get(bestElevatorID)
                    .getDestinations().size() && !anotherElevatorOnRoute) {
                bestElevatorID = tempElevatorID;
            }
        }

        // Add destination to best elevators destinations.
        System.out.println(floor);
        this.elevatorStatuses.get(bestElevatorID).addDestination(floor);

        final int distance = this.elevatorStatuses.get(bestElevatorID).getCurrentFloor() - floor;
        final ElevatorAction direction = distance < 0 ? ElevatorAction.MOVE_UP : ElevatorAction.MOVE_DOWN;

        return new BestElevator(bestElevatorID, Math.abs(distance), direction);
    }

    private int getStopsBetween(final ElevatorStatus elevatorStatus, final int floor) {
        int floorsBetween = 0;

        for (int destination : elevatorStatus.getDestinations()) {
            if (destination < floor && destination > elevatorStatus.getCurrentFloor() ||
                    destination > floor && destination < elevatorStatus.getCurrentFloor()) {
                floorsBetween++;
            }
        }
        return floorsBetween;
    }

    public HashMap<Integer, ElevatorStatus> getElevatorStatuses() {
        return this.elevatorStatuses;
    }

    public void addElevatorStatus(final int elevatorID, final ElevatorStatus elevatorStatus) {
        this.elevatorStatuses.put(elevatorID, elevatorStatus);
    }

    public SchedulerState getState() {
        return this.state;
    }
}

/**
 * Represents the elevator that is closest to a specific floor, and how many
 * floors it must travel to reach it.
 */
final class BestElevator {
    public final int id;
    public final int numFloors;
    public final ElevatorAction direction;

    public BestElevator(final int id, final int numFloors, final ElevatorAction direction) {
        this.id = id;
        this.numFloors = numFloors;
        this.direction = direction;
    }
}
