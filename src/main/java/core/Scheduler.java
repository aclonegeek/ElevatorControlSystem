package core;

import java.util.ArrayDeque;

public class Scheduler implements Runnable {
    private ElevatorSubsystem elevatorSubsystem;
    private FloorSubsystem floorSubsystem;

    private ArrayDeque<FloorData> elevatorEvents;
    private ArrayDeque<FloorData> floorEvents;
    
    // Used for testing purposes.
    private int floorDataCount;

    public Scheduler() {
        this.elevatorEvents = new ArrayDeque<>();
        this.floorEvents = new ArrayDeque<>();
        this.floorDataCount = 0;
    }

    @Override
    public void run() {
        
    }

    public void registerElevatorSubsystem(final ElevatorSubsystem elevatorSubsystem) {
        this.elevatorSubsystem = elevatorSubsystem;
    }

    public void registerFloorSubsystem(final FloorSubsystem floorSubsystem) {
        this.floorSubsystem = floorSubsystem;
    }

    public synchronized void addFloorEvent(final FloorData floorData) {
        this.floorEvents.add(floorData);
        this.notifyAll();
        floorDataCount++;
        System.out.println("Floor adds FloorData.");
    }

    public synchronized void addElevatorEvent(final FloorData elevatorData) {
        this.elevatorEvents.add(elevatorData);
        this.notifyAll();
        floorDataCount++;
        System.out.println("Elevator adds FloorData.");
    }

    public synchronized FloorData removeFloorEvent() {
        while (this.floorEvents.isEmpty()) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                System.err.println(e);
            }
        }

        floorDataCount++;
        System.out.println("Elevator gets FloorData.");
        return this.floorEvents.removeFirst();
    }

    public synchronized FloorData removeElevatorEvent() {
        while (this.elevatorEvents.isEmpty()) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                System.err.println(e);
            }
        }

        floorDataCount++;
        System.out.println("Floor gets FloorData.");
        return this.elevatorEvents.removeFirst();
    }
    
    public int getFloorDataCount() {
        return this.floorDataCount;
    }
}
