package scheduler;

import elevator.ElevatorAction;
import elevator.ElevatorState;
import junit.framework.TestCase;

public class BestElevatorTest extends TestCase {
    public void testBestElevator() {
        final Scheduler scheduler = new Scheduler();

        // idle elevator on floor 9
        final ElevatorStatus elevatorStatus1 = new ElevatorStatus(1, scheduler, ElevatorState.IDLE_DOOR_OPEN, 9);

        // upward Moving elevator on floor 2 stopping at floors 3 and 4
        final ElevatorStatus elevatorStatus2 = new ElevatorStatus(2, scheduler, ElevatorState.MOVING_UP, 2);
        elevatorStatus2.addDestination(3);
        elevatorStatus2.addDestination(4);
        elevatorStatus2.addDestination(10);

        // upward moving elevator on floor 2 stopping at floor 5
        final ElevatorStatus elevatorStatus3 = new ElevatorStatus(3, scheduler, ElevatorState.MOVING_UP, 1);
        elevatorStatus3.addDestination(5);
        elevatorStatus3.addDestination(10);

        // downward moving elevator on floor 8 moving to floor 2
        final ElevatorStatus elevatorStatus4 = new ElevatorStatus(4, scheduler, ElevatorState.MOVING_DOWN, 8);
        elevatorStatus4.addDestination(6);

        // add ElevatorStatus to ElevatorStatus HashMap
        scheduler.addElevatorStatus(1, elevatorStatus1);
        scheduler.addElevatorStatus(2, elevatorStatus2);
        scheduler.addElevatorStatus(3, elevatorStatus3);
        scheduler.addElevatorStatus(4, elevatorStatus4);

       
        BestElevator target = new BestElevator(3, 5, ElevatorAction.MOVE_UP);
        BestElevator predicted = scheduler.getBestElevator(6, ElevatorState.MOVING_UP);
        assertEquals(target.id, predicted.id);
        assertEquals(target.numFloors, predicted.numFloors);
        
        scheduler.getElevatorStatuses().remove(2);
        scheduler.getElevatorStatuses().remove(3);
        
        
        // assert we find the elevator with least total stops if there is no elevator
        // going same direction
        target = new BestElevator(1, 0, ElevatorAction.STOP_MOVING);
        predicted = scheduler.getBestElevator(9, ElevatorState.MOVING_DOWN);
        assertEquals(target.id, predicted.id);
        assertEquals(target.numFloors, predicted.numFloors);
        scheduler.getElevatorStatuses().remove(1);

        // assert we find the elevator with least total stops if there is no elevator
        // going same direction
        target = new BestElevator(4, 1, ElevatorAction.MOVE_DOWN);
        predicted = scheduler.getBestElevator(9, ElevatorState.MOVING_DOWN);
        assertEquals(target.id, predicted.id);
        assertEquals(target.numFloors, predicted.numFloors);
        
    }
}
