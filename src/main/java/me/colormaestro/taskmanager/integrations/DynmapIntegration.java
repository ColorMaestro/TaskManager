package me.colormaestro.taskmanager.integrations;

import me.colormaestro.taskmanager.model.Task;

import java.util.List;

public interface DynmapIntegration {

    /**
     * Adds new marker on map to mark task in progress state ({@link me.colormaestro.taskmanager.enums.TaskStatus#DOING}).
     *
     * @param task for which to add marker
     * @throws IllegalArgumentException if task is missing id or is not in in-progress state.
     */
    void addInProgressTask(Task task);

    /**
     * Updated icon of task marker to represent finished task ({@link me.colormaestro.taskmanager.enums.TaskStatus#FINISHED}).
     *
     * @param key of marker to update
     */
    void markTaskAsFinished(String key);

    /**
     * Removes marker from map.
     *
     * @param key of marker to remove
     */
    void removeActiveTask(String key);

    /**
     * Overwrites actives tasks - basically ensures that currently displayed active tasks are cleared and replaced by
     * specified ones. This means only tasks in-progress ({@link me.colormaestro.taskmanager.enums.TaskStatus#DOING})
     * and finished tasks ({@link me.colormaestro.taskmanager.enums.TaskStatus#FINISHED}) must be replaced on map.
     *
     * @param tasks which to render as active
     */
    void overwriteActiveTasks(List<Task> tasks);

    /**
     * Overwrites idle tasks - basically ensures that currently displayed idle tasks are cleared and replaced by
     * specified ones. This means only tasks in-progress ({@link me.colormaestro.taskmanager.enums.TaskStatus#DOING})
     * which are in this state for more than 30 days since their creation must be replaced on map.
     *
     * @param tasks which to render as idle
     */
    void overwriteIdleTasks(List<Task> tasks);
}
