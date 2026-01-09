package me.colormaestro.taskmanager.commands;

import me.colormaestro.taskmanager.utils.RunnablesCreator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

public class VisitTask implements CommandExecutor {
    private final BukkitScheduler scheduler = Bukkit.getScheduler();
    private final RunnablesCreator creator;

    public VisitTask(RunnablesCreator creator) {
        this.creator = creator;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can't be run from console.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "You must specify task id.");
            return true;
        }

        try {
            Plugin plugin = creator.getPlugin();
            int taskId = Integer.parseInt(args[0]);
            scheduler.runTaskAsynchronously(plugin, creator.teleportPlayerToTask(player, taskId));
            scheduler.runTaskAsynchronously(plugin, creator.givePlayerAssignmentBook(player, taskId));
        } catch (NumberFormatException ex) {
            scheduler.runTask(creator.getPlugin(),
                    () -> player.sendMessage(ChatColor.RED + "Task ID must be numerical value!"));
        }
        return true;
    }
}
