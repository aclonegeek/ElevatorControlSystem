package scheduler;

import java.util.ArrayDeque;

import floor.FloorData;

/**
 * Scheduler subsystem which coordinates the elevator and floor subsystems.
 * Acts as a middleman by routing FloorData events between them.
 */
public class Scheduler implements Runnable {
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

    /**
     * Adds a floor event to the Scheduler, which it will later send to the ElevatorSubsystem.
     *
     * @param floorData the FloorData to store in the queue
     */
    public synchronized void addFloorEvent(final FloorData floorData) {
        this.floorEvents.add(floorData);
        this.notifyAll();
        floorDataCount++;
        System.out.println("Floor adds FloorData.");
    }

    /**
     * Adds an elevator event to the Scheduler, which it will later send to the FloorSubsystem.
     *
     * @param floorData the FloorData to store in the queue
     */
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
