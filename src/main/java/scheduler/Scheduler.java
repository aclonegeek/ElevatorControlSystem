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
        System.out.println("Running scheduler...\n");

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

        // data[1] - elevator ID
        // data[2] - Elevator.Request
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
        // data[3] - elevator state
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

        this.elevatorStatuses.get(id).stopTimer();
        this.elevatorStatuses.get(id).setCurrentFloor(floor);

        if (!this.elevatorStatuses.get(id).getDestinations().contains(floor)) {
            this.elevatorStatuses.get(id).startTimer();
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
    public void sendElevatorAction(final int id, final ElevatorAction action) {
        if (action == ElevatorAction.MOVE_UP || action == ElevatorAction.MOVE_DOWN) {
            this.elevatorStatuses.get(id).startTimer();
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
        System.out.println("Registering elevator " + id + ".");
        // All elevators start at the ground floor.
        this.elevatorStatuses.put(id, new ElevatorStatus(id, this, ElevatorState.IDLE_DOOR_OPEN, 0));
    }
    
    
    //Reroute Elevator
    public void rerouteFaultedElevator(int id, ElevatorState state) {
        ElevatorStatus faultedStatus = this.elevatorStatuses.remove(id);
        int floor = faultedStatus.getCurrentFloor(); 
        int bestElevatorID = findElevator(floor, state);
        this.elevatorStatuses.get(bestElevatorID).addDestinations(faultedStatus.getDestinations());
    }
    
    //Find best elevator for a current floor
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
            
            if(tempElevatorStatus.getState() == state && state == ElevatorState.MOVING_UP && floor >= tempElevatorStatus.getCurrentFloor()) {
                if (tempStopsBetween <= bestStopsBetween) {
                    bestElevatorID = tempElevatorID;
                    onPathElevator = true;
                } 
            } 
            
            else if (tempElevatorStatus.getState() == state && state == ElevatorState.MOVING_DOWN && floor <= tempElevatorStatus.getCurrentFloor()) {
                if (tempStopsBetween <= bestStopsBetween) {
                    bestElevatorID = tempElevatorID;
                    onPathElevator = true;
                }
            }
        }
        
        
        if (onPathElevator == false) {
           
            for (final Entry<Integer, ElevatorStatus> entry : this.elevatorStatuses.entrySet()) {
                
                tempElevatorID = entry.getKey();
                tempElevatorStatus = entry.getValue();
                
                // Best elevator is idle.
                if (tempElevatorStatus.getState() == ElevatorState.DOOR_CLOSED_FOR_IDLING ||
                        tempElevatorStatus.getState() == ElevatorState.IDLE_DOOR_OPEN) {
                    
                    if(bestElevatorID == -69) {
                        bestElevatorID = tempElevatorID;
                        idleElevator = true;
                    }
                    else if(getDistanceBetween(tempElevatorID, floor) <= getDistanceBetween(bestElevatorID, floor)) {
                        bestElevatorID = tempElevatorID;
                        idleElevator = true;
                    }
                }
            }
            
        }
           
        if(!onPathElevator && !idleElevator) {
            for (final Entry<Integer, ElevatorStatus> entry : this.elevatorStatuses.entrySet()) {
                tempElevatorID = entry.getKey();
                tempElevatorStatus = entry.getValue();
            
                if(bestElevatorID == -69) {
                    bestElevatorID = tempElevatorID;
                }
                
                // Best elevator is one the one with least stops between.
                else if (tempElevatorStatus.getDestinations().size() <= this.elevatorStatuses.get(bestElevatorID)
                        .getDestinations().size() && !idleElevator && !onPathElevator) {
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
        int bestElevatorID = findElevator(floor, state);

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
