package me.colormaestro.taskmanager.integrations;

import me.colormaestro.taskmanager.model.Task;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public class EmptyHologramsOperator implements DecentHologramsIntegration {
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
}
