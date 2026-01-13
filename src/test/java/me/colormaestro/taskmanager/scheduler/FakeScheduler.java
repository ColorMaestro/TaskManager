package me.colormaestro.taskmanager.scheduler;

public class FakeScheduler implements Scheduler {
    @Override
    public void runTask(Runnable task) {
        task.run();
    }

    @Override
    public void runTaskAsynchronously(Runnable task) {
        task.run();
    }
}
