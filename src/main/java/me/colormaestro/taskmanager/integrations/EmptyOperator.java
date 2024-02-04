package me.colormaestro.taskmanager.integrations;

import me.colormaestro.taskmanager.model.Task;
import org.bukkit.Location;

import java.util.List;

public class EmptyOperator implements DecentHologramsIntegration, DynmapIntegration {
    @Override
    public void establishTasksHologram(String key, String memberName, Location location) {

    }

    @Override
    public boolean hologramExists(String key) {
        return true;
    }

    @Override
    public void teleportHologram(String key, Location location) {

    }

    @Override
    public void setTasks(String key, List<Task> tasks) {

    }

    @Override
    public void addInProgressTask(Task task, Location location) {

    }

    @Override
    public void updateTaskFinishedMarkerIcon(String key) {

    }

    @Override
    public void removeTaskMarker(String key) {

    }
}
