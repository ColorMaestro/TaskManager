package me.colormaestro.taskmanager.integrations;

import me.colormaestro.taskmanager.model.Task;
import org.bukkit.Location;

public interface DynmapIntegration {

    /**
     * Adds new marker on map to mark task in progress state ({@link me.colormaestro.taskmanager.enums.TaskStatus#DOING}).
     *
     * @param task     for which to add marker
     * @param location where to create marker
     * @throws IllegalArgumentException if task is missing id or is not in in-progress state.
     */
    void addInProgressTask(Task task, Location location);

    /**
     * Updated icon of task marker to represent finished task ({@link me.colormaestro.taskmanager.enums.TaskStatus#FINISHED}).
     *
     * @param key of marker to update
     */
    void updateTaskFinishedMarkerIcon(String key);

    /**
     * Removes marker from map.
     *
     * @param key of marker to remove
     */
    void removeTaskMarker(String key);
}
