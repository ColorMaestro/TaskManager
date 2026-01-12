package me.colormaestro.taskmanager.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class ProductionScheduler implements Scheduler {
    private final Plugin plugin;

    public ProductionScheduler(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void runTask(Runnable task) {
        Bukkit.getScheduler().runTask(plugin, task);
    }

    @Override
    public void runTaskAsynchronously(Runnable task) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }
}
