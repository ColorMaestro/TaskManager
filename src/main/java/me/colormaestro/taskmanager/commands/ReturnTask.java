package me.colormaestro.taskmanager.commands;

import me.colormaestro.taskmanager.data.DataAccessException;
import me.colormaestro.taskmanager.integrations.DiscordOperator;
import me.colormaestro.taskmanager.data.MemberDAO;
import me.colormaestro.taskmanager.data.TaskDAO;
import me.colormaestro.taskmanager.integrations.DecentHologramsIntegration;
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
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;

public class ReturnTask implements CommandExecutor {
    private final BukkitScheduler scheduler = Bukkit.getScheduler();
    private final TaskDAO taskDAO;
    private final MemberDAO memberDAO;
    private final DecentHologramsIntegration decentHolograms;

    public ReturnTask(TaskDAO taskDAO, MemberDAO memberDAO, DecentHologramsIntegration decentHolograms) {
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

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "You must specify task id.");
            return true;
        }

        Plugin plugin = Bukkit.getPluginManager().getPlugin("TaskManager");
        boolean force = args.length == 2 && args[1].equals("force");
        scheduler.runTaskAsynchronously(plugin, () -> {
            try {
                int taskId = Integer.parseInt(args[0]);
                taskDAO.returnTask(taskId, force);
                Task task = taskDAO.findTask(taskId);
                List<Task> activeTasks = taskDAO.fetchPlayersActiveTasks(task.getAssigneeID());
                Member assignee = memberDAO.findMember(task.getAssigneeID());
                scheduler.runTask(plugin, () -> {
                    player.sendMessage(ChatColor.GREEN + "Task returned.");
                    decentHolograms.setTasks(assignee.getUuid(), activeTasks);

                    boolean messageSent = MessageSender.sendMessageIfOnline(
                            assignee.getUuid(),
                            ChatColor.GOLD + player.getName() + " has returned your task."
                    );

                    if (!messageSent && assignee.getDiscordID() != null) {
                        DiscordOperator.getInstance().taskReturned(assignee.getDiscordID(), player.getName(), task);
                    }
                });
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
