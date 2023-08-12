package me.colormaestro.taskmanager.commands;

import me.colormaestro.taskmanager.data.HologramLayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Establish implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can't be run from console");
            return true;
        }

        if (!Bukkit.getPluginManager().isPluginEnabled("DecentHolograms")) {
            sender.sendMessage(ChatColor.RED + "✖ This command works only if DecentHolograms plugin is installed on the server.");
            return true;
        }

        Player player = (Player) sender;
        String uuid = player.getUniqueId().toString();
        if (HologramLayer.getInstance().hologramExists(uuid)) {
            HologramLayer.getInstance().teleportHologram(player);
        } else {
            HologramLayer.getInstance().establishTasksHologram(player);
        }
        return true;
    }
}
