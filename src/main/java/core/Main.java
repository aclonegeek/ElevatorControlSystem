package core;

import elevator.Elevator;
import floor.FloorSubsystem;
import scheduler.Scheduler;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        final Scheduler scheduler = new Scheduler();
        final FloorSubsystem floorSubsystem = new FloorSubsystem(scheduler, 0);
        final Elevator elevator = new Elevator(0, scheduler);

        Thread s = new Thread(scheduler);
        Thread f = new Thread(floorSubsystem);
        Thread e = new Thread(elevator);

        s.start();
        f.start();
        e.start();

        floorSubsystem.addFloorRequest("/floorData.txt");

        Thread.sleep(100);

        // Our test file ends at floor 6.
        System.out.println(6 == elevator.getSubsystem().getCurrentFloor());

        // These are deprecated, but we don't currently handle thread interrupts, and we just want to kill the threads, so stop is good enough for now.
        s.stop();
        f.stop();
        e.stop();
    }
}
