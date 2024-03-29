package me.colormaestro.taskmanager.integrations;

import me.colormaestro.taskmanager.model.Task;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public interface DecentHologramsIntegration {
    /**
     * Creates hologram for player at his current position.
     *
     * @param player for which to create hologram
     * @throws IllegalArgumentException if hologram for this player already exists
     */
    void establishTasksHologram(Player player);

    /**
     * Checks whether hologram with given key exists.
     *
     * @param key of holograms
     * @return true if exists, false otherwise
     */
    boolean hologramExists(String key);

    /**
     * Teleports hologram to location. Rises height of location by 2 before teleport.
     *
     * @param key of hologram to teleport
     * @param location at which to teleport the hologram
     * @throws IllegalArgumentException if hologram with specified key does not exist
     */
    void teleportHologram(String key, Location location);

    /**
     * Formats given tasks on hologram.
     *
     * @param key of hologram on which to put tasks
     * @param tasks to put on hologram
     * @throws IllegalArgumentException if hologram is not found
     */
    void setTasks(String key, List<Task> tasks);
}
