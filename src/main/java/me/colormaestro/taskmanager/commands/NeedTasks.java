package me.colormaestro.taskmanager.commands;

import me.colormaestro.taskmanager.data.TaskDAO;
import me.colormaestro.taskmanager.model.BasicMemberInfo;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;

public class NeedTasks implements CommandExecutor {
    private final Plugin plugin;
    private final TaskDAO taskDAO;

    public NeedTasks(Plugin plugin, TaskDAO taskDAO) {
        this.plugin = plugin;
        this.taskDAO = taskDAO;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                int limit = args.length > 0 ? Integer.parseInt(args[0]) : 0;
                List<BasicMemberInfo> members = taskDAO
                        .fetchMembersDashboardInfo()
                        .stream()
                        .filter(basicMemberInfo -> basicMemberInfo.doing() <= limit)
                        .toList();
                Bukkit.getScheduler().runTask(plugin, () -> sendMessage(sender, members));
            } catch (SQLException ex) {
                Bukkit.getScheduler().runTask(plugin, () -> sender.sendMessage(ChatColor.RED + ex.getMessage()));
                ex.printStackTrace();
            } catch (NumberFormatException ex) {
                Bukkit.getScheduler().runTask(plugin,
                        () -> sender.sendMessage(ChatColor.RED + "Limit must be numerical value!"));
            }
        });
        return true;
    }

    private void sendMessage(CommandSender sender, List<BasicMemberInfo> members) {
        if (members.isEmpty()) {
            sender.sendMessage(ChatColor.GREEN + "Everyone has enough tasks");
            return;
        }
        sender.sendMessage(ChatColor.GOLD + "-=-=-=- Members running out of tasks -=-=-=-");
        for (BasicMemberInfo memberInfo : members) {
            sender.sendMessage(memberInfo.ign() + ChatColor.ITALIC + " (" + memberInfo.doing() + " tasks in progress)");
        }
    }
}
