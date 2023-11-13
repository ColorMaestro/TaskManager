package me.colormaestro.taskmanager.integrations;

import org.bukkit.Location;

public interface DynmapIntegration {

    /**
     * Adds new marker on map to mark task in progress state ({@link me.colormaestro.taskmanager.enums.TaskStatus#DOING}).
     *
     * @param key under which to store marker
     * @param label short description of marker
     * @param location where to create marker
     */
    void addTaskInProgressMarker(String key, String label, Location location);

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
