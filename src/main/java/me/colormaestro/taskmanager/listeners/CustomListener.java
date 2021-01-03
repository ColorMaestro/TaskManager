package me.colormaestro.taskmanager.listeners;

import me.colormaestro.taskmanager.data.HologramLayer;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class CustomListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        String uuid = event.getPlayer().getUniqueId().toString();
        if (!HologramLayer.getInstance().hologramExists(uuid)) {
            event.getPlayer().sendMessage(ChatColor.DARK_AQUA +
                    "█ Your personal hologram has not been established yet                                     █");
            event.getPlayer().sendMessage(ChatColor.DARK_AQUA +
                    "█ To do so stand on the the place, where you want to have it and issue command /establish █");
        }
    }
}
