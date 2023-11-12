package me.colormaestro.taskmanager.commands;

import me.colormaestro.taskmanager.data.DataAccessException;
import me.colormaestro.taskmanager.integrations.DiscordOperator;
import me.colormaestro.taskmanager.data.MemberDAO;
import me.colormaestro.taskmanager.data.TaskDAO;
import me.colormaestro.taskmanager.enums.TaskStatus;
import me.colormaestro.taskmanager.integrations.DecentHologramsIntegration;
import me.colormaestro.taskmanager.model.Member;
import me.colormaestro.taskmanager.model.Task;
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
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can't be run from console.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "You must specify task id and player.");
            return true;
        }

        Plugin plugin = Bukkit.getPluginManager().getPlugin("TaskManager");
        Player p = (Player) sender;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                int id = Integer.parseInt(args[0]);
                Task task = taskDAO.findTask(id);
                if (task.getStatus() == TaskStatus.PREPARED) {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        p.sendMessage(ChatColor.RED + "The task is in prepared state thus transfering is not possible");
                    });
                    return;
                }
                int oldAssigneeID = task.getAssigneeID();
                Member oldAssignee = memberDAO.findMember(oldAssigneeID);
                Member newAssignee = memberDAO.findMember(args[1]);
                taskDAO.updateTaskAssignee(id, newAssignee.getId());
                List<Task> activeTasksOldAssignee = taskDAO.fetchPlayersActiveTasks(oldAssigneeID);
                List<Task> activeTasksNewAssignee = taskDAO.fetchPlayersActiveTasks(newAssignee.getId());
                Bukkit.getScheduler().runTask(plugin, () -> {
                    p.sendMessage(ChatColor.GREEN + "Task transferred.");
                    decentHolograms.setTasks(oldAssignee.getUuid(), activeTasksOldAssignee);
                    decentHolograms.setTasks(newAssignee.getUuid(), activeTasksNewAssignee);

                    // Firstly we try to notify the assignees in game
                    boolean messageSentOldAssignee = false;
                    boolean messageSentNewAssignee = false;
                    for (Player target : Bukkit.getOnlinePlayers()) {
                        if (target.getUniqueId().toString().equals(oldAssignee.getUuid())) {
                            target.sendMessage(ChatColor.GOLD + p.getName() +
                                    " has transferred task " + id + " to " + args[1] + ".");
                            messageSentOldAssignee = true;
                        }

                        if (target.getUniqueId().toString().equals(newAssignee.getUuid())) {
                            target.sendMessage(ChatColor.GOLD + p.getName() +
                                    " has transferred task " + id + " to you.");
                            messageSentNewAssignee = true;
                        }
                    }

                    // If the assignees are not online, sent them message to discord
                    if (!messageSentOldAssignee && oldAssignee.getDiscordID() != null) {
                        DiscordOperator.getInstance().taskTransferred(oldAssignee.getDiscordID(), p.getName(),
                                oldAssignee.getIgn(), args[1], task, true);
                    }

                    if (!messageSentNewAssignee && newAssignee.getDiscordID() != null) {
                        DiscordOperator.getInstance().taskTransferred(newAssignee.getDiscordID(), p.getName(),
                                oldAssignee.getIgn(), args[1], task, false);
                    }
                });
            } catch (SQLException ex) {
                Bukkit.getScheduler().runTask(plugin,
                        () -> p.sendMessage(ChatColor.RED + ex.getMessage()));
                ex.printStackTrace();
            } catch (NumberFormatException ignored) {
                Bukkit.getScheduler().runTask(plugin,
                        () -> p.sendMessage(ChatColor.RED + "Tasks are marked with numerical values!"));
            } catch (DataAccessException ignored) {
                Bukkit.getScheduler().runTask(plugin,
                        () -> p.sendMessage(ChatColor.RED + "Invalid task ID or new assignee!"));
            }
        });
        return true;
    }
}
