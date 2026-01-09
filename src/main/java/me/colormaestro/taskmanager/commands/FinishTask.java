package me.colormaestro.taskmanager.commands;

import me.colormaestro.taskmanager.data.DataAccessException;
import me.colormaestro.taskmanager.data.MemberDAO;
import me.colormaestro.taskmanager.data.TaskDAO;
import me.colormaestro.taskmanager.integrations.DecentHologramsIntegration;
import me.colormaestro.taskmanager.integrations.DiscordOperator;
import me.colormaestro.taskmanager.integrations.DynmapIntegration;
import me.colormaestro.taskmanager.model.Member;
import me.colormaestro.taskmanager.model.Task;
import me.colormaestro.taskmanager.scheduler.Scheduler;
import me.colormaestro.taskmanager.utils.MessageSender;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class FinishTask implements CommandExecutor {
    private final Scheduler scheduler;
    private final TaskDAO taskDAO;
    private final MemberDAO memberDAO;
    private final DecentHologramsIntegration decentHolograms;
    private final DynmapIntegration dynmap;

    public FinishTask(Scheduler scheduler, TaskDAO taskDAO, MemberDAO memberDAO, DecentHologramsIntegration decentHolograms,
                      DynmapIntegration dynmap) {
        this.scheduler = scheduler;
        this.taskDAO = taskDAO;
        this.memberDAO = memberDAO;
        this.decentHolograms = decentHolograms;
        this.dynmap = dynmap;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can't be run from console.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "You must specify task id.");
            return true;
        }

        UUID uuid = player.getUniqueId();
        scheduler.runTaskAsynchronously(() -> {
            try {
                Member assignee = memberDAO.findMember(uuid);
                int taskId = Integer.parseInt(args[0]);
                taskDAO.finishTask(taskId, assignee.getId());
                List<Task> activeTasks = taskDAO.fetchPlayersActiveTasks(assignee.getId());
                Task task = taskDAO.findTask(taskId);
                Member advisor = memberDAO.findMember(task.getAdvisorID());
                scheduler.runTask(() -> {
                    player.sendMessage(ChatColor.GREEN + "Task finished.");
                    decentHolograms.setTasks(assignee.getUuid(), activeTasks);
                    dynmap.updateTaskFinishedMarkerIcon(String.valueOf(taskId));

                    boolean messageSent = MessageSender.sendMessageIfOnline(
                            advisor.getUuid(),
                            ChatColor.GREEN + player.getName() + " finished task " + taskId
                    );

                    if (!messageSent && advisor.getDiscordID() != null) {
                        DiscordOperator.getInstance().taskFinished(advisor.getDiscordID(), player.getName(), task);
                    }
                });
            } catch (SQLException | DataAccessException ex) {
                scheduler.runTask(() -> player.sendMessage(ChatColor.RED + ex.getMessage()));
                ex.printStackTrace();
            } catch (NumberFormatException ex) {
                scheduler.runTask(() -> player.sendMessage(ChatColor.RED + "Task ID must be numerical value!"));
            }
        });
        return true;
    }
}
