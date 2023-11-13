package me.colormaestro.taskmanager.integrations;

import me.colormaestro.taskmanager.model.Task;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public class EmptyOperator implements DecentHologramsIntegration, DynmapIntegration {
    @Override
    public void establishTasksHologram(Player player) {

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
    public void addTaskInProgressMarker(String key, String label, Location location) {

    }

    @Override
    public void updateTaskFinishedMarkerIcon(String key) {

    }

    @Override
    public void removeTaskMarker(String key) {

    }
}
