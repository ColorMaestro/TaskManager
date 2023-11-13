package me.colormaestro.taskmanager.commands;

import me.colormaestro.taskmanager.data.MemberDAO;
import me.colormaestro.taskmanager.data.TaskDAO;
import me.colormaestro.taskmanager.model.MemberInProgressTasksInfo;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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
        Player p = (Player) sender;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                List<MemberInProgressTasksInfo> members = taskDAO.fetchMembersWithFewTasks();
                Bukkit.getScheduler().runTask(plugin, () -> sendMessage(p, members));
            } catch (SQLException ex) {
                Bukkit.getScheduler().runTask(plugin, () -> p.sendMessage(ChatColor.RED + ex.getMessage()));
                ex.printStackTrace();
            }
        });
        return true;
    }

    private void sendMessage(Player player, List<MemberInProgressTasksInfo> members) {
        if (members.isEmpty()) {
            player.sendMessage(ChatColor.GREEN + "Everyone has enough tasks");
            return;
        }
        player.sendMessage(ChatColor.GOLD + "-=-=-=- Members running out of tasks -=-=-=-");
        for (MemberInProgressTasksInfo memberInfo : members) {
            player.sendMessage(memberInfo.ign() + ChatColor.ITALIC + " (" + memberInfo.doing() + " tasks in progress)");
        }
    }
}
