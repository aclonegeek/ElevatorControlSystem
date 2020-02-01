package core;

/**
 * @summary Floor subsystem simulates the functionality of a particular floor
 */
public class FloorSubsystem implements Runnable {
    private enum ButtonState {
        UP, DOWN, UNPRESSED
    }

    private enum ElevatorState {
        UP, DOWN, IDLE, NONE
    }

    private final Scheduler scheduler;
    private ButtonState buttonState;
    private ElevatorState elevatorState;
    private final int number;

    public FloorSubsystem(final Scheduler scheduler, final int number) {
        this.scheduler = scheduler;
        this.buttonState = ButtonState.UNPRESSED;
        this.elevatorState = ElevatorState.NONE;
        this.number = number;
    }

    public void setButtonState(final ButtonState state) {
        this.buttonState = state;
    }
    
    public ButtonState getButtonState(){
        return this.buttonState;
    }
    
    
    public void setElevatorState(ElevatorState state) {
        this.elevatorState = state;
    }

    public ElevatorState getElevatorState() {
        return this.elevatorState;
    }
    
    public int getFloorNumber() {
        return this.number;
    }
    
    // Run
    @Override
    public void run() {
        this.scheduler.registerFloorSubsystem(this);
        //ArrayList<FloorData> floorRequests = FloorReader(this.path).getFloorRequests();
        boolean running = true;
        while (true) {
            /*FloorData firstFloorRequest = floorRequests.get(0);
            if(simTime == firstFloorRequest.getTime()){
                floorRequests.remove(0);
                this.scheduler.addFloorEvent(floorData);
            }*/
        }
    }

}
