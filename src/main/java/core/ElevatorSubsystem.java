package core;

public class ElevatorSubsystem implements Runnable {

    private final ElevatorData data;

    public ElevatorSubsystem(ElevatorData dataInput){
        this.data = dataInput;
    }
    public synchronized void run(){
        while(true){

            if(floorSubsystem.floorRequests(data) != null){
                schedulerSubsystem.sendFloorRequest(data, floorSubsystem.requests(data));
                //turn on the button light
            }

            if(schedulerSubsystem.floorRequests(data) != null){
                if(schedulerSubsystem.floorRequests(data) != data.getCurrentFloor()){
                    schedulerSubsystem.moveElevatorMotor(data);
                    data.setCurrentFloor() = schedulerSubsystem.floorRequests(data);
                    schedulerSubsystem.openElevatorDoor(data);
                    schedulerSubsystem.closeElevatorDoor(data);
                }
            }

            try{
                Thread.sleep(1000);
            }catch(Exception e){
                System.out.println("Elevator " + data.getElevatorId() + " has woken up");
                return;
            }
        }
    }
}
