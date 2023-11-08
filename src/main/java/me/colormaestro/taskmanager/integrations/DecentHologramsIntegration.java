package me.colormaestro.taskmanager.integrations;

import me.colormaestro.taskmanager.model.Task;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public interface DecentHologramsIntegration {
    void establishTasksHologram(Player player);

    boolean hologramExists(String key);

    void teleportHologram(String key, Location location);

    void setTasks(String key, List<Task> tasks);
}
