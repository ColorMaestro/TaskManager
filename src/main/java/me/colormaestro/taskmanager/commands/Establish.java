package me.colormaestro.taskmanager.commands;

import me.colormaestro.taskmanager.data.DataAccessException;
import me.colormaestro.taskmanager.integrations.HologramLayer;
import me.colormaestro.taskmanager.data.MemberDAO;
import me.colormaestro.taskmanager.data.TaskDAO;
import me.colormaestro.taskmanager.model.Member;
import me.colormaestro.taskmanager.model.Task;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.sql.SQLException;
import java.util.List;

public class Establish implements CommandExecutor {
    private final TaskDAO taskDAO;
    private final MemberDAO memberDAO;

    public Establish(TaskDAO taskDAO, MemberDAO memberDAO) {
        this.taskDAO = taskDAO;
        this.memberDAO = memberDAO;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can't be run from console");
            return true;
        }

        if (!Bukkit.getPluginManager().isPluginEnabled("DecentHolograms")) {
            sender.sendMessage(ChatColor.RED + "✖ This command works only if DecentHolograms plugin is installed on the server.");
            return true;
        }

        String uuid = player.getUniqueId().toString();
        if (HologramLayer.getInstance().hologramExists(uuid)) {
            HologramLayer.getInstance().teleportHologram(uuid, player.getLocation());
        } else {
            Plugin plugin = Bukkit.getPluginManager().getPlugin("TaskManager");
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    Member member = memberDAO.findMember(player.getUniqueId());
                    List<Task> membersTasks = taskDAO.fetchPlayersActiveTasks(member.getId());
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        HologramLayer.getInstance().establishTasksHologram(player);
                        HologramLayer.getInstance().setTasks(uuid, membersTasks);
                    });
                } catch (SQLException | DataAccessException e) {
                    e.printStackTrace();
                }
            });
        }
        return true;
    }
}
