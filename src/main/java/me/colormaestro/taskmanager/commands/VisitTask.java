package me.colormaestro.taskmanager.commands;

import me.colormaestro.taskmanager.data.PlayerDAO;
import me.colormaestro.taskmanager.data.TaskDAO;
import me.colormaestro.taskmanager.listeners.SharedRunnables;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class VisitTask implements CommandExecutor {
    private final TaskDAO taskDAO;
    private final PlayerDAO playerDAO;

    public VisitTask(TaskDAO taskDAO, PlayerDAO playerDAO) {
        this.taskDAO = taskDAO;
        this.playerDAO = playerDAO;
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

        Plugin plugin = Bukkit.getPluginManager().getPlugin("TaskManager");
        String taskId = args[0];
        Bukkit.getScheduler().runTaskAsynchronously(plugin,
                SharedRunnables.teleportPlayerToTask(plugin, taskDAO, player, taskId));
        Bukkit.getScheduler().runTaskAsynchronously(plugin,
                SharedRunnables.givePlayerAssignmentBook(plugin, taskDAO, playerDAO, player, taskId));
        return true;
    }
}
