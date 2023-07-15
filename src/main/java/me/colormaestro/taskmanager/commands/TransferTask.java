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
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;

public class TransferTask implements CommandExecutor {
    private final TaskDAO taskDAO;
    private final PlayerDAO playerDAO;

    public TransferTask(TaskDAO taskDAO, PlayerDAO playerDAO) {
        this.taskDAO = taskDAO;
        this.playerDAO = playerDAO;
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
                int oldAssigneeID = task.getAssigneeID();
                int newAssigneeID = playerDAO.getPlayerID(args[1]);
                taskDAO.updateTaskAssignee(id, newAssigneeID);
                List<Task> activeTasksOldAssignee = taskDAO.fetchPlayersActiveTasks(oldAssigneeID);
                List<Task> activeTasksNewAssignee = taskDAO.fetchPlayersActiveTasks(newAssigneeID);
                String oldAssigneeUUID = playerDAO.getPlayerUUID(oldAssigneeID);
                String newAssigneeUUID = playerDAO.getPlayerUUID(newAssigneeID);
                long discordOldAssigneeID = playerDAO.getDiscordUserID(oldAssigneeUUID);
                long discordNewAssigneeID = playerDAO.getDiscordUserID(newAssigneeUUID);
                String oldAssigneeIGN = playerDAO.getPlayerIGN(oldAssigneeID);
                Bukkit.getScheduler().runTask(plugin,
                        () -> {
                            p.sendMessage(ChatColor.GREEN + "Task transferred.");
                            if (Bukkit.getPluginManager().isPluginEnabled("Holograms")) {
                                HologramLayer.getInstance().setTasks(oldAssigneeUUID, activeTasksOldAssignee);
                                HologramLayer.getInstance().setTasks(newAssigneeUUID, activeTasksNewAssignee);
                            }

                            // Firstly we try to notify the assignees in game
                            boolean messageSentOldAssignee = false;
                            boolean messageSentNewAssignee = false;
                            for (Player target : Bukkit.getOnlinePlayers()) {
                                if (target.getUniqueId().toString().equals(oldAssigneeUUID)) {
                                    target.sendMessage(ChatColor.GOLD + p.getName() +
                                            " has transferred task " + id + " to " + args[1] + ".");
                                    messageSentOldAssignee = true;
                                }

                                if (target.getUniqueId().toString().equals(newAssigneeUUID)) {
                                    target.sendMessage(ChatColor.GOLD + p.getName() +
                                            " has transferred task " + id + " to you.");
                                    messageSentNewAssignee = true;
                                }
                            }

                            // If the assignees are not online, sent them message to discord
                            if (!messageSentOldAssignee) {
                                DiscordManager.getInstance().taskTransferred(discordOldAssigneeID, p.getName(),
                                        oldAssigneeIGN, args[1], task,true);
                            }

                            if (!messageSentNewAssignee) {
                                DiscordManager.getInstance().taskTransferred(discordNewAssigneeID, p.getName(),
                                        oldAssigneeIGN, args[1], task,false);
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
