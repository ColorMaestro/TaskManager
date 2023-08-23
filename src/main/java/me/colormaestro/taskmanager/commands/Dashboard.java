package me.colormaestro.taskmanager.commands;

import me.colormaestro.taskmanager.data.TaskDAO;
import me.colormaestro.taskmanager.listeners.SharedRunnables;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class Dashboard implements CommandExecutor {
    private final Plugin plugin;
    private final TaskDAO taskDAO;

    public Dashboard(Plugin plugin, TaskDAO taskDAO) {
        this.plugin = plugin;
        this.taskDAO = taskDAO;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can't be run from console.");
            return true;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, SharedRunnables.showDashboardView(plugin, taskDAO, player, 1));

        return true;
    }
}
