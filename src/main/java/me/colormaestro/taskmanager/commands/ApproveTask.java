package me.colormaestro.taskmanager.commands;

import me.colormaestro.taskmanager.data.DataAccessException;
import me.colormaestro.taskmanager.data.DiscordManager;
import me.colormaestro.taskmanager.data.HologramLayer;
import me.colormaestro.taskmanager.data.PlayerDAO;
import me.colormaestro.taskmanager.data.TaskDAO;
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
import java.util.function.BiConsumer;

public class ApproveTask implements CommandExecutor {
    private final TaskDAO taskDAO;
    private final PlayerDAO playerDAO;

    public ApproveTask(TaskDAO taskDAO, PlayerDAO playerDAO) {
        this.taskDAO = taskDAO;
        this.playerDAO = playerDAO;
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
        String sid = args[0];
        boolean force = args.length == 2 && args[1].equals("force");
        Bukkit.getScheduler().runTaskAsynchronously(plugin,
                () -> {
                    try {
                        int id = Integer.parseInt(sid);
                        taskDAO.approveTask(id, force);
                        Task task = taskDAO.findTask(id);
                        List<Task> activeTasks = taskDAO.fetchPlayersActiveTasks(task.getAssigneeID());
                        String assigneeUUID = playerDAO.getPlayerUUID(task.getAssigneeID());
                        long discordUserID = playerDAO.getDiscordUserID(assigneeUUID);
                        Bukkit.getScheduler().runTask(plugin,
                                () -> {
                                    p.sendMessage(ChatColor.GREEN + "Task approved.");
                                    HologramLayer.getInstance().setTasks(assigneeUUID, activeTasks);

                                    // Firstly we try to notify the assignee in game
                                    boolean messageSent = false;
                                    for (Player target : Bukkit.getOnlinePlayers()) {
                                        if (target.getUniqueId().toString().equals(assigneeUUID)) {
                                            target.sendMessage(ChatColor.GREEN + p.getName() + " has accepted your task. Great Job!");
                                            target.playSound(target.getLocation(),
                                                    "minecraft:record.taskaccepted", 10, 1);
                                            messageSent = true;
                                            break;
                                        }
                                    }

                                    // If the assignee is not online, sent him message to discord
                                    if (!messageSent) {
                                        DiscordManager.getInstance().taskApproved(discordUserID, p.getName(), task);
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
