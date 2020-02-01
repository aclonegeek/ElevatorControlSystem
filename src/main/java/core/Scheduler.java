package core;

import java.util.ArrayList;
import java.util.HashMap;

public class Scheduler implements Runnable {
    private ElevatorSubsystem elevatorSubsystem;
    private FloorSubsystem floorSubsystem;

    private ArrayList<ElevatorData> elevatorEvents;
    private ArrayList<FloorData> floorEvents;

    public Scheduler() {
        this.elevatorEvents = new ArrayList<>();
        this.floorEvents = new ArrayList<>();
    }

    @Override
    public void run() {
        while (true) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                System.err.println(e);
            }
        }
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
    }

    public synchronized void addElevatorEvent(final ElevatorData elevatorData) {
        this.elevatorEvents.add(elevatorData);
        this.notifyAll();
    }

    public synchronized FloorData getFloorEvent() {
        while (this.floorEvents.isEmpty()) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                System.err.println(e);
            }
        }

        return this.floorEvents.remove(0);
    }

    public synchronized ElevatorData getElevatorEvent() {
        while (this.elevatorEvents.isEmpty()) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                System.err.println(e);
            }
        }

        return this.elevatorEvents.remove(0);
    }
}
