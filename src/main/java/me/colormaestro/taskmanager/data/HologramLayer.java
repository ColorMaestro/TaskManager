package me.colormaestro.taskmanager.data;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import me.colormaestro.taskmanager.enums.TaskStatus;
import me.colormaestro.taskmanager.model.Task;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.List;

public class HologramLayer {
    private static HologramLayer instance;

    private HologramLayer() {
    }

    public static void instantiate() {
        if (instance == null) {
            instance = new HologramLayer();
        }
    }

    public static HologramLayer getInstance() {
        return instance;
    }

    public void establishTasksHologram(Player player) {
        Location location = player.getLocation();
        location.setY(location.getY() + 2);
        List<String> lines = List.of(ChatColor.BLUE + "" + ChatColor.BOLD + player.getName() + "'s task list");
        DHAPI.createHologram(player.getUniqueId().toString(), location, true, lines);
        player.sendMessage(ChatColor.GREEN + "✔ Your visual task list has been established!");
        player.sendMessage(ChatColor.GREEN + "ℹ If you want to move it somewhere else, do"
                + ChatColor.GOLD + "" + ChatColor.BOLD + " /establish" + ChatColor.GREEN + " there");
    }

    public boolean hologramExists(String key) {
        Hologram hologram = DHAPI.getHologram(key);
        return hologram != null;
    }

    public void teleportHologram(Player player) {
        Location location = player.getLocation();
        location.setY(location.getY() + 2);
        DHAPI.moveHologram(player.getUniqueId().toString(), location);
    }

    public void setTasks(String uuid, List<Task> tasks) {
        Hologram hologram = DHAPI.getHologram(uuid);
        if (hologram == null) {
            return;
        }
        List<String> lines = new LinkedList<>();
        for (Task task : tasks) {
            ChatColor color = task.getStatus() == TaskStatus.FINISHED ? ChatColor.GREEN : ChatColor.GOLD;
            String line = color + "[" + task.getId() + "] " + ChatColor.WHITE + task.getTitle();
            lines.add(line);
        }
        DHAPI.setHologramLines(hologram, lines);
    }
}
