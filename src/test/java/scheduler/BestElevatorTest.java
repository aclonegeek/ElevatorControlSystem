package scheduler;

import elevator.ElevatorAction;
import elevator.ElevatorState;
import junit.framework.TestCase;

public class BestElevatorTest extends TestCase {
    public void testBestElevator() {
        final Scheduler scheduler = new Scheduler();

        // idle elevator on floor 9
        ElevatorStatus elevatorStatus1 = new ElevatorStatus(ElevatorState.IDLE_DOOR_OPEN, 9);

        // upward Moving elevator on floor 2 stopping at floors 3 and 4
        ElevatorStatus elevatorStatus2 = new ElevatorStatus(ElevatorState.MOVING_UP, 2);
        elevatorStatus2.addDestination(3);
        elevatorStatus2.addDestination(4);
        elevatorStatus2.addDestination(10);

        // upward moving elevator on floor 2 stopping at floor 5
        ElevatorStatus elevatorStatus3 = new ElevatorStatus(ElevatorState.MOVING_UP, 1);
        elevatorStatus3.addDestination(5);
        elevatorStatus3.addDestination(10);

        // downward moving elevator on floor 8 moving to floor 2
        ElevatorStatus elevatorStatus4 = new ElevatorStatus(ElevatorState.MOVING_DOWN, 8);
        elevatorStatus4.addDestination(6);

        // add ElevatorStatus to ElevatorStatus HashMap
        scheduler.addElevatorStatus(1, elevatorStatus1);
        scheduler.addElevatorStatus(2, elevatorStatus2);
        scheduler.addElevatorStatus(3, elevatorStatus3);
        scheduler.addElevatorStatus(4, elevatorStatus4);

        // assert we get the idle Elevator first
        BestElevator target = new BestElevator(1, 3, ElevatorAction.MOVE_DOWN);
        BestElevator predicted = scheduler.getBestElevator(6, ElevatorState.MOVING_DOWN);
        assertEquals(target.id, predicted.id);
        assertEquals(target.numFloors, predicted.numFloors);

        // remove the first elevator because it has priority
        scheduler.getElevatorStatuses().remove(1);

        // assert we find an elevator going the same direction with the least stops
        // before
        target = new BestElevator(3, 5, ElevatorAction.MOVE_UP);
        predicted = scheduler.getBestElevator(6, ElevatorState.MOVING_UP);
        assertEquals(target.id, predicted.id);
        assertEquals(target.numFloors, predicted.numFloors);

        // assert we find the elevator with least total stops if there is no elevator
        // going same direction
        target = new BestElevator(4, 1, ElevatorAction.MOVE_DOWN);
        predicted = scheduler.getBestElevator(9, ElevatorState.MOVING_DOWN);
        assertEquals(target.id, predicted.id);
        assertEquals(target.numFloors, predicted.numFloors);
    }
}
