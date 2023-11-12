package me.colormaestro.taskmanager.commands;

import me.colormaestro.taskmanager.data.DataAccessException;
import me.colormaestro.taskmanager.integrations.DiscordOperator;
import me.colormaestro.taskmanager.data.MemberDAO;
import me.colormaestro.taskmanager.data.TaskDAO;
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

public class ReturnTask implements CommandExecutor {
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
        String sid = args[0];
        boolean force = args.length == 2 && args[1].equals("force");
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                int id = Integer.parseInt(sid);
                taskDAO.returnTask(id, force);
                Task task = taskDAO.findTask(id);
                List<Task> activeTasks = taskDAO.fetchPlayersActiveTasks(task.getAssigneeID());
                Member assignee = memberDAO.findMember(task.getAssigneeID());
                Bukkit.getScheduler().runTask(plugin, () -> {
                    p.sendMessage(ChatColor.GREEN + "Task returned.");
                    decentHolograms.setTasks(assignee.getUuid(), activeTasks);

                    // Firstly we try to notify the assignee in game
                    boolean messageSent = false;
                    for (Player target : Bukkit.getOnlinePlayers()) {
                        if (target.getUniqueId().toString().equals(assignee.getUuid())) {
                            target.sendMessage(ChatColor.GOLD + p.getName() + " has returned your task.");
                            messageSent = true;
                            break;
                        }
                    }

                    // If the assignee is not online, sent him message to discord
                    if (!messageSent && assignee.getDiscordID() != null) {
                        DiscordOperator.getInstance().taskReturned(assignee.getDiscordID(), p.getName(), task);
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
