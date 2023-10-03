package me.colormaestro.taskmanager.commands;

import me.colormaestro.taskmanager.data.MemberDAO;
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
    private final MemberDAO memberDAO;

    public Dashboard(Plugin plugin, TaskDAO taskDAO, MemberDAO memberDAO) {
        this.plugin = plugin;
        this.taskDAO = taskDAO;
        this.memberDAO = memberDAO;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can't be run from console.");
            return true;
        }

        if (args.length > 0) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, SharedRunnables.showActiveTasksView(plugin, taskDAO, memberDAO, player, args[0], 1));
            return true;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, SharedRunnables.showDashboardView(plugin, taskDAO, player, 1));

        return true;
    }
}
