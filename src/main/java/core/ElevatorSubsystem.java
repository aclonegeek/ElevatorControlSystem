package core;

public class ElevatorSubsystem implements Runnable {

    private final ElevatorData data;
    private final Scheduler scheduler;
    private FloorData targetFloor;

    public ElevatorSubsystem(ElevatorData dataInput, Scheduler schedulerInput){

        this.data = dataInput;
        this.scheduler = schedulerInput;
    }
    public synchronized void run() {
        while(true){
            scheduler.addFloorEvent(scheduler.getFloorEvent());
        }
    }

}
