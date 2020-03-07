package classes;

import scheduler.Scheduler;

public class RunnableScheduler implements Runnable {
    final Scheduler scheduler;

    public RunnableScheduler() {
        this.scheduler = new Scheduler();
    }

    public void run() {
        this.scheduler.run();
    }

    public Scheduler getScheduler() {
        return this.scheduler;
    }
}
