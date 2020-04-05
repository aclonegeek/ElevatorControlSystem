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
import elevator.ElevatorFault;
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
        System.out.println("[scheduler] Running scheduler...\n");

        while (true) {
            final DatagramPacket packet = this.receive();
            this.handleMessage(packet.getData());
        }
    }

    private DatagramPacket receive() {
        final byte data[] = new byte[7];
        final DatagramPacket packet = new DatagramPacket(data, data.length);

        try {
            this.receiveSocket.receive(packet);
        } catch (final IOException e) {
            System.err.println(e);
            System.exit(1);
        }

        return packet;
    }

    /**
     * Handles messages from the {@link Floor}, {@link Elevator}, and {@link ArrivalSensor}.
     *
     * @param data the message to parse
     */
    private void handleMessage(final byte[] data) {
        this.state = State.HANDLING_MESSAGE;

        System.out.println("[scheduler] Received: " + Arrays.toString(data));

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
            System.err.println("[scheduler] Received invalid bytes.");
            break;
        }

        this.state = State.WAITING;
    }

    private void handleFloorMessage(final byte[] data) {
        System.out.print("[scheduler] Floor message: " +
                         "Floor = " + data[1] + ", " +
                         "Destination Floor = " + data[3] + ", " +
                         "ButtonState = " + ButtonState.values()[data[4]]);

        if (data[5] != 0) {
            System.out.print(", ElevatorFault = " + ElevatorFault.values()[data[5 - 1]] +
                             ", Fault Floor = " + data[6]);
        }

        System.out.println();

        switch (Floor.Request.values[data[2]]) {
        // data[1] - floor that made the request
        // data[2] - floor request type
        // data[3] - destination floor
        // data[4] - button state (up or down)
        // data[5] - optional, indicates a fault
        // data[6] - optional, indicates floor the fault will occur
        case REQUEST:
            // If there is a fault, send it off to the elevator system right away.
            // Then we continue processing the rest of the message as normal.
            if (data[5] != 0) {
                this.sendElevatorFault(ElevatorFault.values()[data[5] - 1], data[6]);
            }

            final int destinationFloor = data[3];
            final ElevatorState direction = this.getDirectionFromButtonState(ButtonState.values[data[4]]);

            final BestElevator bestElevator = this.getBestElevator(data[1], direction);
            final ElevatorStatus status = this.elevatorStatuses.get(bestElevator.id);
            if (!status.getDestinations().contains(destinationFloor)) {
                status.addDestination(destinationFloor);
            }

            if (status.getState() == ElevatorState.MOVING_UP ||
                status.getState() == ElevatorState.MOVING_DOWN) {
                break;
            }

            status.startDoorFaultTimerTask();
            this.sendElevatorAction(bestElevator.id, ElevatorAction.CLOSE_DOORS);
            break;
        default:
            System.out.println("[scheduler] Unknown floor message received.");
            break;
        }
    }

    private void handleElevatorMessage(final byte[] data) {
        System.out.print("[scheduler] Elevator message: " +
                         "Elevator ID = " + data[1] + ", " +
                         "ElevatorRequest = " + Elevator.Request.values[data[2]]);

        if (Elevator.Request.values[data[2]] == Elevator.Request.STATE_CHANGED) {
            System.out.print(", ElevatorState = " + ElevatorState.values[data[3]]);
        }

        System.out.println();

        // data[1] - elevator ID
        // data[2] - Elevator.Request
        switch (Elevator.Request.values[data[2]]) {
        case REGISTER:
            this.registerElevator(data[1]);
            System.out.println();
            break;
        case READY: {
            final int id = data[1];
            final ElevatorStatus status = this.elevatorStatuses.get(id);

            if (status.getDestinations().isEmpty()) {
                break;
            }

            status.startDoorFaultTimerTask();
            this.sendElevatorAction(id, ElevatorAction.CLOSE_DOORS);
        }
            break;
        case OPEN_DOORS: {
            final int id = data[1];
            this.elevatorStatuses.get(id).startDoorFaultTimerTask();
            this.sendElevatorAction(id, ElevatorAction.OPEN_DOORS);
            break;
        }
        // data[3] - elevator state
        case STATE_CHANGED: {
            final int id = data[1];
            final ElevatorState state = ElevatorState.values[data[3]];
            final ElevatorStatus status = this.elevatorStatuses.get(id);
            status.setState(state);

            // We can now move the elevator.
            if (state == ElevatorState.DOOR_CLOSED_FOR_MOVING) {
                this.sendElevatorMoveAction(id, status);
            }
        }
            break;
        default:
            System.out.println("[scheduler] Unknown elevator message received.");
            break;
        }
    }

    private void handleArrivalSensorMessage(final byte[] data) {
        System.out.println("[scheduler] Arrival sensor message: " +
                           "Elevator ID = " + data[1] + ", " +
                           "Floor = " + data[2]);

        final int id = data[1];
        final int floor = data[2];
        final ElevatorStatus status = this.elevatorStatuses.get(id);

        status.stopMovementTimerTask();
        status.setCurrentFloor(floor);

        if (!status.getDestinations().contains(floor)) {
            status.startMovementTimerTask();
            return;
        }

        final byte reply[] = { Globals.FROM_SCHEDULER, data[1], (byte) ElevatorAction.STOP_MOVING.ordinal() };
        final DatagramPacket packet =
                new DatagramPacket(reply, reply.length, Globals.IP, Globals.ELEVATOR_PORT);

        this.send(packet);

        status.getDestinations().remove(status.getDestinations().indexOf(floor));
    }

    /**
     * Sends a {@link DatagramPacket} to a port.
     *
     * @param packet the {@link DatagramPacket} to send.
     */
    private void send(final DatagramPacket packet) {
        System.out.println(
                "[scheduler] Sending to port " + packet.getPort() + ": " + Arrays.toString(packet.getData()));
        this.logSendPretty(packet.getData());

        try {
            this.sendSocket.send(packet);
        } catch (final IOException e) {
            System.err.println(e);
            System.exit(1);
        }
    }

    /**
     * Prints out data to be sent in a pretty way.
     *
     * @param data the data to pretty print
     */
    private void logSendPretty(final byte[] data) {
        switch (data[0]) {
        case Globals.FROM_FLOOR:
            System.out.println("[scheduler] Sending: " + "FROM_FLOOR, ElevatorFault = " +
                               ElevatorFault.values()[data[1]] + ", " +
                               data[2]);
            break;
        case Globals.FROM_SCHEDULER:
            System.out.println("[scheduler] Sending: " + "FROM_SCHEDULER, Elevator ID = " +
                               data[1] + ", ElevatorAction = " + ElevatorAction.values()[data[2]]);
            break;
        }

        System.out.println();
    }

    /**
     * Sends an {@link Elevator} an {@link ElevatorAction}.
     *
     * @param id     the {@link Elevator}'s id
     * @param action the {@link ElevatorAction} for the {@link Elevator} to perform
     */
    public void sendElevatorAction(final int id, final ElevatorAction action) {
        if (action == ElevatorAction.MOVE_UP || action == ElevatorAction.MOVE_DOWN) {
            this.elevatorStatuses.get(id).startMovementTimerTask();
        }

        final byte reply[] =
                { Globals.FROM_SCHEDULER, (byte) id, (byte) action.ordinal() };
        final DatagramPacket packet =
                new DatagramPacket(reply, reply.length, Globals.IP, Globals.ELEVATOR_PORT);
        this.send(packet);
    }

    /**
     * Sends an {@link ElevatorFault} to the {@link ElevatorSubsystem}.
     *
     * @param elevatorFault the {@link ElevatorFault}
     * @param floor         the {@link Floor} to have a fault on
     */
    private void sendElevatorFault(final ElevatorFault elevatorFault, final int floor) {
        // The reply is deliberately FROM_FLOOR.
        final byte reply[] =
                { Globals.FROM_FLOOR, (byte) elevatorFault.ordinal(), (byte) floor };
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
        System.out.println("[scheduler] Registering elevator " + id + ".");
        // All elevators start at the ground floor.
        this.elevatorStatuses.put(id, new ElevatorStatus(id, this, ElevatorState.IDLE_DOOR_OPEN, 0));
    }

    /**
     * Sends an {@link ElevatorAction} to an {@link Elevator}.
     *
     * @param id     the {@link Elevator}'s id
     * @param status the {@link Elevator}'s status
     */
    public void sendElevatorMoveAction(final int id, final ElevatorStatus status)  {
        final int distance = status.getCurrentFloor() - status.getDestinations().get(0);
        final ElevatorAction direction = distance < 0 ? ElevatorAction.MOVE_UP : ElevatorAction.MOVE_DOWN;
        this.sendElevatorAction(id, direction);
    }

    /**
     * Reroutes a faulted {@link Elevator}.
     *
     * @param id    the {@link Elevator}'s id
     * @param state the {@link ElevatorState} current state
     */
    public void rerouteFaultedElevator(final int id, final ElevatorState state) {
        final ElevatorStatus faultedStatus = this.elevatorStatuses.remove(id); 
        final int bestElevatorID = findElevator(faultedStatus.getCurrentFloor(), state);
        this.elevatorStatuses.get(bestElevatorID).addDestinations(faultedStatus.getDestinations());
        final int distance = this.elevatorStatuses.get(bestElevatorID).getCurrentFloor() - faultedStatus.getCurrentFloor();
        final ElevatorAction direction = distance < 0 ? ElevatorAction.MOVE_UP : ElevatorAction.MOVE_DOWN;
        this.sendElevatorAction(bestElevatorID, direction);
    }

    /**
     * Finds the best elevator for a floor.
     *
     * @param floor the floor to find the best elevator for
     * @param state the {@link Elevator}'s current state
     */
    private int findElevator(final int floor, final ElevatorState state) {
        int bestElevatorID = -69;
        int bestStopsBetween = 420;
        int tempElevatorID;
        ElevatorStatus tempElevatorStatus;
        boolean idleElevator = false;
        boolean onPathElevator = false;

        for (final Entry<Integer, ElevatorStatus> entry : this.elevatorStatuses.entrySet()) {
            tempElevatorID = entry.getKey();
            tempElevatorStatus = entry.getValue();

            final int tempStopsBetween = getStopsBetween(tempElevatorStatus, floor);

            if (floor >= tempElevatorStatus.getCurrentFloor() && state == ElevatorState.MOVING_UP) {
                if (tempStopsBetween <= bestStopsBetween) {
                    bestElevatorID = tempElevatorID;
                    bestStopsBetween = tempStopsBetween;
                    onPathElevator = true;
                }
            } else if (floor <= tempElevatorStatus.getCurrentFloor() && state == ElevatorState.MOVING_DOWN) {
                if (tempStopsBetween <= bestStopsBetween) {
                    bestElevatorID = tempElevatorID;
                    bestStopsBetween = tempStopsBetween;
                    onPathElevator = true;
                }
            }
        }


        if (!onPathElevator) {
            for (final Entry<Integer, ElevatorStatus> entry : this.elevatorStatuses.entrySet()) {
                tempElevatorID = entry.getKey();
                tempElevatorStatus = entry.getValue();

                // Best elevator is idle.
                if (tempElevatorStatus.getDestinations().size() == 0) {

                    if (bestElevatorID == -69 ||
                        getAbsoluteDistanceBetween(tempElevatorID, floor) <= getAbsoluteDistanceBetween(bestElevatorID, floor)) {
                        bestElevatorID = tempElevatorID;
                        idleElevator = true;
                    }
                }
            }
        }

        if (!onPathElevator && !idleElevator) {
            for (final Entry<Integer, ElevatorStatus> entry : this.elevatorStatuses.entrySet()) {
                tempElevatorID = entry.getKey();
                tempElevatorStatus = entry.getValue();

                if (bestElevatorID == -69) {
                    bestElevatorID = tempElevatorID;
                } // Best elevator is the one with least stops between.
                else if (tempElevatorStatus.getDestinations().size() <= this.elevatorStatuses.get(bestElevatorID)
                         .getDestinations().size() &&
                         !idleElevator &&
                         !onPathElevator) {
                    bestElevatorID = tempElevatorID;
                }
            }
        }

        return bestElevatorID;
    }

    /**
     * Determines the best {@link Elevator} to travel to the specified {@link Floor}.
     *
     * @param floor the floor to go to
     * @param state the direction to travel (up or down)
     *
     * @return the {@link BestElevator} for the job
     */
    public BestElevator getBestElevator(final int floor, final ElevatorState state) {
        final int bestElevatorID = findElevator(floor, state);
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
    private ElevatorState getDirectionFromButtonState(final ButtonState state) {
        switch (state) {
        case UP:
            return ElevatorState.MOVING_UP;
        case DOWN:
            return ElevatorState.MOVING_DOWN;
        default:
            return ElevatorState.DOOR_CLOSED_FOR_IDLING;
        }
    }

    /**
     * Returns the distance between an elevator's current floor and a floor.
     *
     * @param elevatorId
     * @param floor
     *
     * @return the distance between the elevator's current floor and floor
     */
    private int getAbsoluteDistanceBetween(final int elevatorId, final int floor) {
        return Math.abs(this.elevatorStatuses.get(elevatorId).getCurrentFloor() - floor);
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
 * Represents the elevator that the best to travel to a specific floor,
 * how many floors it must travel to reach it, and in which direction.
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
