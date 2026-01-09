package me.colormaestro.taskmanager.scheduler;

/**
 * Adapter for {@link org.bukkit.scheduler.BukkitScheduler}
 */
public interface Scheduler {
    /**
     * Executes task synchronously
     *
     * @param task the task to be run
     */
    void runTask(Runnable task);

    /**
     * Executes task asynchronously
     *
     * @param task the task to be run
     */
    void runTaskAsynchronously(Runnable task);
}
