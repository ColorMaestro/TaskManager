package me.colormaestro.taskmanager.data;

import com.sainttx.holograms.api.Hologram;
import com.sainttx.holograms.api.HologramManager;
import com.sainttx.holograms.api.line.HologramLine;
import com.sainttx.holograms.api.line.TextLine;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;

public class HologramLayer {
    private static HologramLayer instance;
    private HologramManager manager;

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
                ChatColor.YELLOW + "" + ChatColor.BOLD + player.getName() + "'s task list");
        hologram.addLine(line);
        manager.addActiveHologram(hologram);  // Adds to memory
        manager.saveHologram(hologram);  // Adds to YAML file in plugin working directory
        hologram.spawn();  // Shows the hologram (It doesn't show by default, when added this way)
        player.sendMessage(ChatColor.GREEN + "✔ Your personal visual task list has been established!");
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
}
