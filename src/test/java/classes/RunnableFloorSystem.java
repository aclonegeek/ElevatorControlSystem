package classes;

import floor.FloorSystem;

public class RunnableFloorSystem implements Runnable {
    final FloorSystem floorSystem;

    public RunnableFloorSystem(final String filename, final int sleepTime) {
        this.floorSystem = new FloorSystem(filename, sleepTime);
    }

    public void run() {
        this.floorSystem.run();
    }
    
    public FloorSystem getFloorSystem() {
        return this.floorSystem;
    }
}
