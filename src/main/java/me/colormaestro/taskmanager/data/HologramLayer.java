package me.colormaestro.taskmanager.data;

import com.sainttx.holograms.api.Hologram;
import com.sainttx.holograms.api.HologramManager;
import com.sainttx.holograms.api.line.HologramLine;
import com.sainttx.holograms.api.line.TextLine;
import me.colormaestro.taskmanager.enums.TaskStatus;
import me.colormaestro.taskmanager.model.Task;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class HologramLayer {
    private static HologramLayer instance;
    private final HologramManager manager;

    private HologramLayer(HologramManager manager) {
        this.manager = manager;
    }

    public static void instantiate(HologramManager manager) {
        if (instance == null) {
            instance = new HologramLayer(manager);
        }
    }

    public static HologramLayer getInstance() {
        return instance;
    }

    public void establishTasksHologram(Player player) {
        Location location = player.getLocation();
        location.setY(location.getY() + 2);
        Hologram hologram = new Hologram(player.getUniqueId().toString(), location);
        HologramLine line = new TextLine(hologram,
                ChatColor.BLUE + "" + ChatColor.BOLD + player.getName() + "'s task list");
        hologram.addLine(line);
        manager.addActiveHologram(hologram);  // Adds to memory
        manager.saveHologram(hologram);  // Adds to YAML file in plugin working directory
        hologram.spawn();  // Shows the hologram (It doesn't show by default, when added this way)
        player.sendMessage(ChatColor.GREEN + "✔ Your visual task list has been established!");
        player.sendMessage(ChatColor.GREEN + "ℹ If you want to move it somewhere else, do"
                + ChatColor.GOLD + "" + ChatColor.BOLD + " /establish" + ChatColor.GREEN +" there");
    }

    public boolean hologramExists(String key) {
        Map<String, Hologram> map = manager.getActiveHolograms();
        Set<String> names = map.keySet();
        for (String name : names) {
            if (name.equals(key)) {
                return true;
            }
        }
        return false;
    }

    public void teleportHologram(Player player, String key) {
        Map<String, Hologram> map = manager.getActiveHolograms();
        Set<String> names = map.keySet();
        for (String name : names) {
            if (name.equals(key)) {
                Location location = player.getLocation();
                location.setY(location.getY() + 2);
                map.get(key).teleport(location);
                break;
            }
        }
    }

    public void setTasks(String uuid, List<Task> tasks) {
        Hologram hologram = manager.getHologram(uuid);
        if (hologram == null) {
            return;
        }
        HologramLine currentLine;
        while ((currentLine = hologram.getLine(1)) != null) {
            hologram.removeLine(currentLine);
        }
        for (Task task : tasks) {
            ChatColor color = task.getStatus() == TaskStatus.FINISHED ? ChatColor.GREEN : ChatColor.GOLD;
            HologramLine line = new TextLine(hologram, color + "[" + task.getId() + "] "
                    + ChatColor.WHITE + task.getTitle());
            hologram.addLine(line);
        }
        manager.saveHologram(hologram);
    }
}
