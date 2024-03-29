package me.colormaestro.taskmanager.integrations;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import me.colormaestro.taskmanager.enums.TaskStatus;
import me.colormaestro.taskmanager.model.Task;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public class DecentHologramsOperator implements DecentHologramsIntegration {

    @Override
    public void establishTasksHologram(Player player) {
        Location location = player.getLocation();
        location.setY(location.getY() + 2);
        List<String> lines = List.of(ChatColor.BLUE + "" + ChatColor.BOLD + player.getName() + "'s task list");
        DHAPI.createHologram(player.getUniqueId().toString(), location, true, lines);
        player.sendMessage(ChatColor.GREEN + "✔ Your visual task list has been established!");
        player.sendMessage(ChatColor.GREEN + "ℹ If you want to move it somewhere else, do"
                + ChatColor.GOLD + ChatColor.BOLD + " /establish" + ChatColor.GREEN + " there");
    }

    @Override
    public boolean hologramExists(String key) {
        return DHAPI.getHologram(key) != null;
    }

    @Override
    public void teleportHologram(String key, Location location) {
        location.setY(location.getY() + 2);
        DHAPI.moveHologram(key, location);
    }

    @Override
    public void setTasks(String key, List<Task> tasks) {
        Hologram hologram = DHAPI.getHologram(key);
        if (hologram == null) {
            return;
        }
        // We remove all lines except of first one to keep member's name on hologram
        while (hologram.getPage(0).getLine(1) != null) {
            DHAPI.removeHologramLine(hologram, 1);
        }
        for (Task task : tasks) {
            ChatColor color = task.getStatus() == TaskStatus.FINISHED ? ChatColor.GREEN : ChatColor.GOLD;
            String line = color + "[" + task.getId() + "] " + ChatColor.WHITE + task.getTitle();
            DHAPI.addHologramLine(hologram, line);
        }
    }
}
