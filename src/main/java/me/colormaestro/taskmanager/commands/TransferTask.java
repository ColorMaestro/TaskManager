package me.colormaestro.taskmanager.commands;

import me.colormaestro.taskmanager.data.DataAccessException;
import me.colormaestro.taskmanager.data.MemberDAO;
import me.colormaestro.taskmanager.data.TaskDAO;
import me.colormaestro.taskmanager.enums.TaskStatus;
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

public class TransferTask implements CommandExecutor {
    private final TaskDAO taskDAO;
    private final MemberDAO memberDAO;
    private final DecentHologramsIntegration decentHolograms;

    public TransferTask(TaskDAO taskDAO, MemberDAO memberDAO, DecentHologramsIntegration decentHolograms) {
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
            sender.sendMessage(ChatColor.RED + "You must specify task id and player.");
            return true;
        }

        Plugin plugin = Bukkit.getPluginManager().getPlugin("TaskManager");
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                int taskId = Integer.parseInt(args[0]);
                Task task = taskDAO.findTask(taskId);
                if (task.getStatus() == TaskStatus.PREPARED) {
                    Bukkit.getScheduler().runTask(plugin, () ->
                            player.sendMessage(ChatColor.RED + "The task is in prepared state thus transfering is not possible"));
                    return;
                }
                int oldAssigneeID = task.getAssigneeID();
                Member oldAssignee = memberDAO.findMember(oldAssigneeID);
                Member newAssignee = memberDAO.findMember(args[1]);
                taskDAO.updateTaskAssignee(taskId, newAssignee.getId());
                List<Task> activeTasksOldAssignee = taskDAO.fetchPlayersActiveTasks(oldAssigneeID);
                List<Task> activeTasksNewAssignee = taskDAO.fetchPlayersActiveTasks(newAssignee.getId());
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.sendMessage(ChatColor.GREEN + "Task transferred.");
                    decentHolograms.setTasks(oldAssignee.getUuid(), activeTasksOldAssignee);
                    decentHolograms.setTasks(newAssignee.getUuid(), activeTasksNewAssignee);

                    boolean messageSentOldAssignee = MessageSender.sendMessageIfOnline(
                            oldAssignee.getUuid(),
                            ChatColor.GOLD + player.getName() + " has transferred task " + taskId + " to " + args[1] + "."
                    );

                    boolean messageSentNewAssignee = MessageSender.sendMessageIfOnline(
                            newAssignee.getUuid(),
                            ChatColor.GOLD + player.getName() + " has transferred task " + taskId + " to you."
                    );

                    // If the assignees are not online, sent them message to discord
                    if (!messageSentOldAssignee && oldAssignee.getDiscordID() != null) {
                        DiscordOperator.getInstance().taskTransferred(oldAssignee.getDiscordID(), player.getName(),
                                oldAssignee.getIgn(), args[1], task, true);
                    }

                    if (!messageSentNewAssignee && newAssignee.getDiscordID() != null) {
                        DiscordOperator.getInstance().taskTransferred(newAssignee.getDiscordID(), player.getName(),
                                oldAssignee.getIgn(), args[1], task, false);
                    }
                });
            } catch (SQLException ex) {
                Bukkit.getScheduler().runTask(plugin,
                        () -> player.sendMessage(ChatColor.RED + ex.getMessage()));
                ex.printStackTrace();
            } catch (NumberFormatException ignored) {
                Bukkit.getScheduler().runTask(plugin,
                        () -> player.sendMessage(ChatColor.RED + "Task ID must be numerical value!"));
            } catch (DataAccessException ignored) {
                Bukkit.getScheduler().runTask(plugin,
                        () -> player.sendMessage(ChatColor.RED + "Invalid task ID or new assignee!"));
            }
        });
        return true;
    }
}
