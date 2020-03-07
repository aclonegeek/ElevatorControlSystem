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
 * Coordinates elevators and floors.
 */
public class Scheduler {
    public static enum State {
        WAITING, HANDLING_MESSAGE,
    }

    private final HashMap<Integer, ElevatorStatus> elevatorStatuses;

    private State state;

    private DatagramSocket receiveSocket;
    private DatagramSocket sendSocket;

    public static void main(final String args[]) {
        new Scheduler().run();
    }

    public Scheduler() {
        this.elevatorStatuses = new HashMap<>();
        this.state = State.WAITING;

        try {
            this.receiveSocket = new DatagramSocket(Globals.SCHEDULER_PORT);
            this.sendSocket = new DatagramSocket();
        } catch (final SocketException e) {
            System.err.println(e);
            System.exit(1);
        }
    }

    public void run() {
        System.out.println("Running scheduler...\n");

        while (true) {
            final DatagramPacket packet = this.receive();
            this.handleMessage(packet.getData());
        }
    }

    private DatagramPacket receive() {
        final byte data[] = new byte[5];
        final DatagramPacket packet = new DatagramPacket(data, data.length);

        try {
            this.receiveSocket.receive(packet);
        } catch (final IOException e) {
            System.err.println(e);
            System.exit(1);
        }

        return packet;
    }

    private void handleMessage(final byte[] data) {
        if (data.length == 0) {
            System.out.println("Received empty message.");
            return;
        }

        this.state = State.HANDLING_MESSAGE;

        System.out.println("Received: " + Arrays.toString(data));

        switch (data[0]) {
        case Globals.FROM_FLOOR:
            this.handleFloorMessage(data);
            break;
        case Globals.FROM_ELEVATOR:
            this.handleElevatorMessage(data);
            break;
        case Globals.FROM_ARRIVAL_SENSOR:
            this.handleArrivalSensorMessage(data);
            break;
        default:
            System.err.println("Received invalid bytes.");
            break;
        }

        this.state = State.WAITING;
    }

    private void handleFloorMessage(final byte[] data) {
        System.out.println("Handling a floor message.");

        switch (Floor.Request.values[data[2]]) {
        case REQUEST:
            final ElevatorState direction = this.checkDirection(ButtonState.values[data[4]]);
            final BestElevator bestElevator = this.getBestElevator(data[1], direction);

            this.sendElevatorAction(bestElevator.id, ElevatorAction.CLOSE_DOORS);

            this.elevatorStatuses.get(bestElevator.id).addDestination(data[3]);
            this.sendElevatorAction(bestElevator.id, bestElevator.direction);
            break;
        default:
            System.out.println("Unknown floor message received.");
            break;
        }
    }

    private void handleElevatorMessage(final byte[] data) {
        System.out.println("Handling an elevator message.");

        switch (Elevator.Request.values[data[2]]) {
        case REGISTER:
            this.registerElevator(data[1]);
            System.out.println();
            break;
        case READY: {
            final int id = data[1];

            if (this.elevatorStatuses.get(id).getDestinations().isEmpty()) {
                break;
            }

            this.sendElevatorAction(id, ElevatorAction.CLOSE_DOORS);

            // TODO: Tidy this up.
            final int currentFloor = this.elevatorStatuses.get(id).getCurrentFloor();
            final int destination = this.elevatorStatuses.get(id).getDestinations().get(0);
            final int distance = currentFloor - destination;

            final ElevatorAction direction = distance < 0 ? ElevatorAction.MOVE_UP : ElevatorAction.MOVE_DOWN;

            this.sendElevatorAction(id, direction);
        }
            break;
        case OPEN_DOORS:
            this.sendElevatorAction(data[1], ElevatorAction.OPEN_DOORS);
            break;
        case STATE_CHANGED: {
            final int id = data[1];
            final ElevatorState state = ElevatorState.values[data[3]];
            this.elevatorStatuses.get(id).setState(state);
        }
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

    /**
     * Send a {@link DatagramPacket} to a port.
     *
     * @param packet the {@link DatagramPacket} to send.
     */
    private void send(final DatagramPacket packet) {
        System.out.println(
                "Sending to port " + packet.getPort() + ": " + Arrays.toString(packet.getData()) + "\n");

        try {
            this.sendSocket.send(packet);
        } catch (final IOException e) {
            System.err.println(e);
            System.exit(1);
        }
    }

    /**
     * Sends an {@link Elevator} an {@link ElevatorAction}.
     *
     * @param id     the {@link Elevator}'s id
     * @param action the {@link ElevatorAction} for the {@link Elevator} to perform
     */
    private void sendElevatorAction(final int id, final ElevatorAction action) {
        final byte reply[] =
                { Globals.FROM_SCHEDULER, (byte) id, (byte) action.ordinal() };
        final DatagramPacket packet =
                new DatagramPacket(reply, reply.length, Globals.IP, Globals.ELEVATOR_PORT);
        this.send(packet);
    }

    /**
     * Registers an {@link Elevator} with the {@link Scheduler}.
     *
     * @param id the {@link Elevator}'s id
     */
    private void registerElevator(final int id) {
        System.out.println("Registering elevator " + id + ".");
        // All elevators start at the ground floor.
        this.elevatorStatuses.put(id, new ElevatorStatus(ElevatorState.IDLE_DOOR_OPEN, 0));
    }

    // TODO: Change ElevatorState to ElevatorAction.
    public BestElevator getBestElevator(final int floor, final ElevatorState state) {
        int bestElevatorID = -1;
        int tempElevatorID;
        ElevatorStatus tempElevatorStatus;
        boolean anotherElevatorOnRoute = false;
        boolean idleElevator = false;

        for (final Entry<Integer, ElevatorStatus> entry : this.elevatorStatuses.entrySet()) {
            tempElevatorID = entry.getKey();
            tempElevatorStatus = entry.getValue();

            // Init the bestId to the first ID.
            if (bestElevatorID == -1) {
                bestElevatorID = tempElevatorID;
            }

            final int tempStopsBetween = getStopsBetween(tempElevatorStatus, floor);
            final int bestStopsBetween = getStopsBetween(this.elevatorStatuses.get(bestElevatorID), floor);

            // Best elevator is idle.
            if (tempElevatorStatus.getState() == ElevatorState.DOOR_CLOSED_FOR_IDLING ||
                    tempElevatorStatus.getState() == ElevatorState.IDLE_DOOR_OPEN) {
                if(getDistanceBetween(tempElevatorID, floor) <= getDistanceBetween(bestElevatorID, floor)){
                    bestElevatorID = tempElevatorID;
                    idleElevator = true;
                }
            }

            // Best elevator is one moving with the floor on its path going upwards.
            if (tempElevatorStatus.getState() == state && state == ElevatorState.MOVING_UP) {
                if (tempElevatorStatus.getCurrentFloor() < floor && !idleElevator) {
                    if (tempStopsBetween <= bestStopsBetween) {
                        bestElevatorID = tempElevatorID;
                        anotherElevatorOnRoute = true;
                    }
                }
            }

            // Best elevator is one moving with the floor on its path going downwards.
            else if (tempElevatorStatus.getState() == state && state == ElevatorState.MOVING_DOWN && !idleElevator) {
                if (tempElevatorStatus.getCurrentFloor() > floor) {
                    if (tempStopsBetween <= bestStopsBetween) {
                        bestElevatorID = tempElevatorID;
                        anotherElevatorOnRoute = true;
                    }
                }
            }

            // Best elevator is one the one with least stops between.
            if (tempElevatorStatus.getDestinations().size() <= this.elevatorStatuses.get(bestElevatorID)
                    .getDestinations().size() && !anotherElevatorOnRoute && !idleElevator) {
                bestElevatorID = tempElevatorID;
            }
        }

        // Add destination to best elevators destinations.
        this.elevatorStatuses.get(bestElevatorID).addDestination(floor);

        final int distance = this.elevatorStatuses.get(bestElevatorID).getCurrentFloor() - floor;
        final ElevatorAction direction = distance < 0 ? ElevatorAction.MOVE_UP : ElevatorAction.MOVE_DOWN;

        return new BestElevator(bestElevatorID, Math.abs(distance), direction);
    }

    /**
     * Returns the corresponding {@link ElevatorState} for a given {@link ButtonState}.
     *
     * @return the corresponding {@link ElevatorState} for a given {@link ButtonState}
     */
    private ElevatorState checkDirection(final ButtonState state) {
        if (state == ButtonState.UP) {
            return ElevatorState.MOVING_UP;
        } else if (state == ButtonState.DOWN) {
            return ElevatorState.MOVING_DOWN;
        } else {
            return ElevatorState.DOOR_CLOSED_FOR_IDLING;
        }
    }

    private int getDistanceBetween(final int bestElevatorID, final int floor) {
        return Math.abs(this.elevatorStatuses.get(bestElevatorID).getCurrentFloor() - floor);
    }

    private int getStopsBetween(final ElevatorStatus elevatorStatus, final int floor) {
        int floorsBetween = 0;

        for (final int destination : elevatorStatus.getDestinations()) {
            if (destination < floor && destination > elevatorStatus.getCurrentFloor() ||
                    destination > floor && destination < elevatorStatus.getCurrentFloor()) {
                floorsBetween++;
            }
        }
        return floorsBetween;
    }

    /* METHODS USED FOR TESTING */
    public State getState() {
        return this.state;
    }

    public HashMap<Integer, ElevatorStatus> getElevatorStatuses() {
        return this.elevatorStatuses;
    }

    public void addElevatorStatus(final int elevatorID, final ElevatorStatus elevatorStatus) {
        this.elevatorStatuses.put(elevatorID, elevatorStatus);
    }

    public void setElevatorStatusFloor(final int elevatorID, final int floor) {
        this.elevatorStatuses.get(elevatorID).setCurrentFloor(floor);
    }
    
    public void closeSockets() {
        this.receiveSocket.close();
        this.sendSocket.close();
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
