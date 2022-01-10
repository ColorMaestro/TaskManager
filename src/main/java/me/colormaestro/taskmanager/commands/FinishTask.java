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
import java.util.UUID;

public class FinishTask implements CommandExecutor {
    private final TaskDAO taskDAO;
    private final PlayerDAO playerDAO;

    public FinishTask(TaskDAO taskDAO, PlayerDAO playerDAO) {
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
        UUID uuid = p.getUniqueId();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                int assigneeID = playerDAO.getPlayerID(uuid);
                int id = Integer.parseInt(args[0]);
                taskDAO.finishTask(id, assigneeID);
                List<Task> activeTasks = taskDAO.fetchPlayersActiveTasks(assigneeID);
                String assigneeUUID = playerDAO.getPlayerUUID(assigneeID);
                Task task = taskDAO.findTask(id);
                String advisorUUID = playerDAO.getPlayerUUID(task.getAdvisorID());
                long discordUserID = playerDAO.getDiscordUserID(advisorUUID);
                Bukkit.getScheduler().runTask(plugin,
                        () -> {
                            p.sendMessage(ChatColor.GREEN + "Task finished.");
                            if (Bukkit.getPluginManager().isPluginEnabled("Holograms")) {
                                HologramLayer.getInstance().setTasks(assigneeUUID, activeTasks);
                            }

                            // Firstly we try to notify the assigner in game
                            boolean messageSent = false;
                            for (Player target : Bukkit.getOnlinePlayers()) {
                                if (target.getUniqueId().toString().equals(advisorUUID)) {
                                    target.sendMessage(ChatColor.GREEN + p.getName() + " finished task " + id);
                                    target.playSound(target.getLocation(),
                                            "minecraft:record.taskfinished", 10, 1);
                                    messageSent = true;
                                    break;
                                }
                            }

                            // If the assigner is not online, sent him message to discord
                            if (!messageSent) {
                                DiscordManager.getInstance().taskFinished(discordUserID, p.getName(), task);
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
