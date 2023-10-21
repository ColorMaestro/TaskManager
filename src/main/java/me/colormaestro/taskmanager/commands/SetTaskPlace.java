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

import java.sql.SQLException;
import java.util.UUID;

public class SetTaskPlace implements CommandExecutor {
    private final TaskDAO taskDAO;
    private final MemberDAO memberDAO;

    public SetTaskPlace(TaskDAO taskDAO, MemberDAO memberDAO) {
        this.taskDAO = taskDAO;
        this.memberDAO = memberDAO;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can't be run from console.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "You must specify task id.");
            return true;
        }

        Plugin plugin = Bukkit.getPluginManager().getPlugin("TaskManager");
        Player p = (Player) sender;
        UUID uuid = p.getUniqueId();
        String sid = args[0];
        Location location = p.getLocation();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Member assignee = memberDAO.findMember(uuid);
                int id = Integer.parseInt(sid);
                taskDAO.updateTaskCords(id, assignee.getId(), location);
                Bukkit.getScheduler().runTask(plugin,
                        () -> p.sendMessage(ChatColor.GREEN + "Cords updated."));
            } catch (SQLException | DataAccessException | NumberFormatException ex) {
                Bukkit.getScheduler().runTask(plugin,
                        () -> p.sendMessage(ChatColor.RED + ex.getMessage()));
                ex.printStackTrace();
            }
        });
        return true;
    }
}
