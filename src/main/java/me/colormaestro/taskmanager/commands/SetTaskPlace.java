package me.colormaestro.taskmanager.commands;

import me.colormaestro.taskmanager.data.DataAccessException;
import me.colormaestro.taskmanager.data.MemberDAO;
import me.colormaestro.taskmanager.data.TaskDAO;
import me.colormaestro.taskmanager.model.Member;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.sql.SQLException;
import java.util.UUID;

public class SetTaskPlace implements CommandExecutor {
    private final BukkitScheduler scheduler = Bukkit.getScheduler();
    private final TaskDAO taskDAO;
    private final MemberDAO memberDAO;

    public SetTaskPlace(TaskDAO taskDAO, MemberDAO memberDAO) {
        this.taskDAO = taskDAO;
        this.memberDAO = memberDAO;
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
        UUID uuid = player.getUniqueId();
        Location location = player.getLocation();
        scheduler.runTaskAsynchronously(plugin, () -> {
            try {
                Member assignee = memberDAO.findMember(uuid);
                int taskId = Integer.parseInt(args[0]);
                taskDAO.updateTaskCords(taskId, assignee.getId(), location);
                scheduler.runTask(plugin,
                        () -> player.sendMessage(ChatColor.GREEN + "Cords updated."));
            } catch (SQLException | DataAccessException ex) {
                scheduler.runTask(plugin,
                        () -> player.sendMessage(ChatColor.RED + ex.getMessage()));
                ex.printStackTrace();
            } catch (NumberFormatException ex) {
                scheduler.runTask(plugin,
                        () -> player.sendMessage(ChatColor.RED + "Task ID must be numerical value!"));
            }
        });
        return true;
    }
}
