package me.colormaestro.taskmanager.commands;

import me.colormaestro.taskmanager.data.DataAccessException;
import me.colormaestro.taskmanager.data.MemberDAO;
import me.colormaestro.taskmanager.data.TaskDAO;
import me.colormaestro.taskmanager.integrations.DecentHologramsIntegration;
import me.colormaestro.taskmanager.model.Member;
import me.colormaestro.taskmanager.model.Task;
import me.colormaestro.taskmanager.scheduler.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;

public class Establish implements CommandExecutor {
    private final Scheduler scheduler;
    private final TaskDAO taskDAO;
    private final MemberDAO memberDAO;
    private final DecentHologramsIntegration decentHolograms;

    public Establish(Scheduler scheduler, TaskDAO taskDAO, MemberDAO memberDAO, DecentHologramsIntegration decentHolograms) {
        this.scheduler = scheduler;
        this.taskDAO = taskDAO;
        this.memberDAO = memberDAO;
        this.decentHolograms = decentHolograms;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can't be run from console");
            return true;
        }

        if (!Bukkit.getPluginManager().isPluginEnabled("DecentHolograms")) {
            sender.sendMessage(ChatColor.RED + "✖ This command works only if DecentHolograms plugin is installed on the server.");
            return true;
        }

        String uuid = player.getUniqueId().toString();
        if (decentHolograms.hologramExists(uuid)) {
            decentHolograms.teleportHologram(uuid, player.getLocation());
        } else {
            scheduler.runTaskAsynchronously(() -> {
                try {
                    Member member = memberDAO.findMember(player.getUniqueId());
                    List<Task> membersTasks = taskDAO.fetchPlayersActiveTasks(member.getId());
                    scheduler.runTask(() -> {
                        decentHolograms.establishTasksHologram(player);
                        decentHolograms.setTasks(uuid, membersTasks);
                    });
                } catch (SQLException | DataAccessException e) {
                    e.printStackTrace();
                }
            });
        }
        return true;
    }
}
