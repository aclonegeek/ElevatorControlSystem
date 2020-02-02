package core;

import java.util.ArrayDeque;

public class Scheduler implements Runnable {
    private ElevatorSubsystem elevatorSubsystem;
    private FloorSubsystem floorSubsystem;

    private ArrayDeque<FloorData> elevatorEvents;
    private ArrayDeque<FloorData> floorEvents;

    public Scheduler() {
        this.elevatorEvents = new ArrayDeque<>();
        this.floorEvents = new ArrayDeque<>();
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
        System.out.println("Floor adds FloorData.");
    }

    public synchronized void addElevatorEvent(final FloorData elevatorData) {
        this.elevatorEvents.add(elevatorData);
        this.notifyAll();
        System.out.println("Elevator adds FloorData.");
    }

    public synchronized FloorData getFloorEvent() {
        while (this.floorEvents.isEmpty()) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                System.err.println(e);
            }
        }

        System.out.println("Elevator gets FloorData.");
        return this.floorEvents.removeFirst();
    }

    public synchronized FloorData getElevatorEvent() {
        while (this.elevatorEvents.isEmpty()) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                System.err.println(e);
            }
        }

        System.out.println("Floor gets FloorData.");
        return this.elevatorEvents.removeFirst();
    }
}
