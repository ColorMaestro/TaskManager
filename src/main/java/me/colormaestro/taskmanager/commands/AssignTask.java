package me.colormaestro.taskmanager.commands;

import me.colormaestro.taskmanager.data.DataAccessException;
import me.colormaestro.taskmanager.data.MemberDAO;
import me.colormaestro.taskmanager.data.TaskDAO;
import me.colormaestro.taskmanager.integrations.DecentHologramsIntegration;
import me.colormaestro.taskmanager.integrations.DiscordOperator;
import me.colormaestro.taskmanager.model.Member;
import me.colormaestro.taskmanager.model.Task;
import me.colormaestro.taskmanager.utils.MessageSender;
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
import java.util.UUID;

public class AssignTask implements CommandExecutor {
    private final Plugin plugin;
    private final TaskDAO taskDAO;
    private final MemberDAO memberDAO;
    private final DecentHologramsIntegration decentHolograms;

    public AssignTask(Plugin plugin, TaskDAO taskDAO, MemberDAO memberDAO, DecentHologramsIntegration decentHolograms) {
        this.plugin = plugin;
        this.taskDAO = taskDAO;
        this.memberDAO = memberDAO;
        this.decentHolograms = decentHolograms;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can't be run from console.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "You must specify member and task.");
            return true;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String ign = args[0];
            UUID uuid = player.getUniqueId();
            Member assignee, advisor;
            try {
                assignee = memberDAO.findMember(ign);
                advisor = memberDAO.findMember(uuid);
            } catch (SQLException ex) {
                Bukkit.getScheduler().runTask(plugin, () -> player.sendMessage(ChatColor.RED + ex.getMessage()));
                ex.printStackTrace();
                return;
            } catch (DataAccessException ignored) {
                Bukkit.getScheduler().runTask(plugin, () -> player.sendMessage(ChatColor.GOLD + "Player " + ign +
                        " is not registered as member. Use" + ChatColor.DARK_AQUA + " /addmember " + ign +
                        ChatColor.GOLD + " for adding player as member, then you can add tasks."));
                return;
            }

            try {
                int taskId = Integer.parseInt(args[1]);
                taskDAO.assignTask(taskId, assignee.getId(), advisor.getId());
                Task task = taskDAO.findTask(taskId);
                List<Task> activeTasks = taskDAO.fetchPlayersActiveTasks(assignee.getId());
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.sendMessage(ChatColor.GREEN + "Task assigned.");
                    decentHolograms.setTasks(assignee.getUuid(), activeTasks);

                    boolean messageSent = MessageSender.sendMessageIfOnline(
                            assignee.getUuid(),
                            ChatColor.GOLD + "You have new task from " + player.getName()
                    );

                    if (!messageSent && assignee.getDiscordID() != null) {
                        DiscordOperator.getInstance().taskCreated(assignee.getDiscordID(), player.getName(), task);
                    }
                });
            } catch (SQLException | DataAccessException ex) {
                Bukkit.getScheduler().runTask(plugin,
                        () -> player.sendMessage(ChatColor.RED + ex.getMessage()));
                ex.printStackTrace();
            } catch (NumberFormatException ex) {
                Bukkit.getScheduler().runTask(plugin,
                        () -> player.sendMessage(ChatColor.RED + "Task ID must be numerical value!"));
            }
        });

        return true;
    }
}
