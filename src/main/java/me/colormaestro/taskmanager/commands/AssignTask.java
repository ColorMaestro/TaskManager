package me.colormaestro.taskmanager.commands;

import me.colormaestro.taskmanager.data.DataAccessException;
import me.colormaestro.taskmanager.data.DiscordManager;
import me.colormaestro.taskmanager.data.HologramLayer;
import me.colormaestro.taskmanager.data.PlayerDAO;
import me.colormaestro.taskmanager.data.TaskDAO;
import me.colormaestro.taskmanager.enums.TaskStatus;
import me.colormaestro.taskmanager.model.AdvisedTask;
import me.colormaestro.taskmanager.model.Task;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class AssignTask implements CommandExecutor {
    private final Plugin plugin;
    private final TaskDAO taskDAO;
    private final PlayerDAO playerDAO;

    public AssignTask(Plugin plugin, TaskDAO taskDAO, PlayerDAO playerDAO) {
        this.plugin = plugin;
        this.taskDAO = taskDAO;
        this.playerDAO = playerDAO;
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
            int assigneeID, advisorID;
            try {
                assigneeID = playerDAO.getPlayerID(ign);
                advisorID = playerDAO.getPlayerID(uuid);
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
                taskDAO.assignTask(taskId, assigneeID, advisorID);
                Task task = taskDAO.findTask(taskId);
                List<Task> activeTasks = taskDAO.fetchPlayersActiveTasks(assigneeID);
                String assigneeUUID = playerDAO.getPlayerUUID(assigneeID);
                long discordUserID = playerDAO.getDiscordUserID(assigneeUUID);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.sendMessage(ChatColor.GREEN + "Task assigned.");

                    // Firstly we try to notify the assignee in game
                    boolean messageSent = false;
                    if (Bukkit.getPluginManager().isPluginEnabled("DecentHolograms")) {
                        HologramLayer.getInstance().setTasks(assigneeUUID, activeTasks);
                    }
                    for (Player target : Bukkit.getOnlinePlayers()) {
                        if (target.getUniqueId().toString().equals(assigneeUUID)) {
                            target.sendMessage(ChatColor.GOLD + "You have new task from " + player.getName());
                            target.playSound(target.getLocation(),
                                    "minecraft:record.newtask", 10, 1);
                            messageSent = true;
                            break;
                        }
                    }

                    // If the assignee is not online, sent him message to discord
                    if (!messageSent) {
                        DiscordManager.getInstance().taskCreated(discordUserID, player.getName(), task);
                    }
                });
            } catch (SQLException | IllegalArgumentException | DataAccessException ex) {
                Bukkit.getScheduler().runTask(plugin,
                        () -> player.sendMessage(ChatColor.RED + ex.getMessage()));
                ex.printStackTrace();
            }
        });

        return true;
    }
}
