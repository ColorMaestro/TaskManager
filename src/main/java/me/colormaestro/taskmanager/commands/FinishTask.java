package me.colormaestro.taskmanager.commands;

import me.colormaestro.taskmanager.data.DataAccessException;
import me.colormaestro.taskmanager.integrations.DiscordOperator;
import me.colormaestro.taskmanager.data.MemberDAO;
import me.colormaestro.taskmanager.data.TaskDAO;
import me.colormaestro.taskmanager.integrations.DecentHologramsIntegration;
import me.colormaestro.taskmanager.integrations.DynmapIntegration;
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
import java.util.UUID;

public class FinishTask implements CommandExecutor {
    private final TaskDAO taskDAO;
    private final MemberDAO memberDAO;
    private final DecentHologramsIntegration decentHolograms;
    private final DynmapIntegration dynmap;

    public FinishTask(TaskDAO taskDAO, MemberDAO memberDAO, DecentHologramsIntegration decentHolograms,
                      DynmapIntegration dynmap) {
        this.taskDAO = taskDAO;
        this.memberDAO = memberDAO;
        this.decentHolograms = decentHolograms;
        this.dynmap = dynmap;
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
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Member assignee = memberDAO.findMember(uuid);
                int id = Integer.parseInt(args[0]);
                taskDAO.finishTask(id, assignee.getId());
                List<Task> activeTasks = taskDAO.fetchPlayersActiveTasks(assignee.getId());
                Task task = taskDAO.findTask(id);
                Member advisor = memberDAO.findMember(task.getAdvisorID());
                Bukkit.getScheduler().runTask(plugin, () -> {
                    p.sendMessage(ChatColor.GREEN + "Task finished.");
                    decentHolograms.setTasks(assignee.getUuid(), activeTasks);
                    dynmap.updateTaskFinishedMarkerIcon(String.valueOf(id));

                    // Firstly we try to notify the assigner in game
                    boolean messageSent = false;
                    for (Player target : Bukkit.getOnlinePlayers()) {
                        if (target.getUniqueId().toString().equals(advisor.getUuid())) {
                            target.sendMessage(ChatColor.GREEN + p.getName() + " finished task " + id);
                            target.playSound(target.getLocation(),
                                    "minecraft:record.taskfinished", 10, 1);
                            messageSent = true;
                            break;
                        }
                    }

                    // If the assigner is not online, sent him message to discord
                    if (!messageSent && advisor.getDiscordID() != null) {
                        DiscordOperator.getInstance().taskFinished(advisor.getDiscordID(), p.getName(), task);
                    }
                });
            } catch (SQLException | DataAccessException | NumberFormatException ex) {
                Bukkit.getScheduler().runTask(plugin,
                        () -> p.sendMessage(ChatColor.RED + ex.getMessage()));
                ex.printStackTrace();
            }
        });
        return true;
    }
}
