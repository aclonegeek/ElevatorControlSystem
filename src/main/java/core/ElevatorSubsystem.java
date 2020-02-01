package core;

public class ElevatorSubsystem implements Runnable {

    private final ElevatorData data;
    private final Scheduler scheduler;

    public ElevatorSubsystem(ElevatorData dataInput, Scheduler schedulerInput){

        this.data = dataInput;
        this.scheduler = schedulerInput;
    }
    public synchronized void run() {

        scheduler.registerElevatorSubsystem(this.data);

        while(true){
            scheduler.addElevatorEvent(scheduler.getFloorEvent());
        }
    }

}
