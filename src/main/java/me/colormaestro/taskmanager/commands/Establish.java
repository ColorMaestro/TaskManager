package me.colormaestro.taskmanager.commands;

import me.colormaestro.taskmanager.data.DataAccessException;
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

public class Establish implements CommandExecutor {
    private final TaskDAO taskDAO;
    private final PlayerDAO playerDAO;

    public Establish(TaskDAO taskDAO, PlayerDAO playerDAO) {
        this.taskDAO = taskDAO;
        this.playerDAO = playerDAO;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can't be run from console");
            return true;
        }

        if (!Bukkit.getPluginManager().isPluginEnabled("DecentHolograms")) {
            sender.sendMessage(ChatColor.RED + "✖ This command works only if DecentHolograms plugin is installed on the server.");
            return true;
        }

        Player player = (Player) sender;
        String uuid = player.getUniqueId().toString();
        if (HologramLayer.getInstance().hologramExists(uuid)) {
            HologramLayer.getInstance().teleportHologram(player);
        } else {
            Plugin plugin = Bukkit.getPluginManager().getPlugin("TaskManager");
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    int id = playerDAO.getPlayerID(player.getUniqueId());
                    List<Task> membersTasks = taskDAO.fetchPlayersActiveTasks(id);
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        HologramLayer.getInstance().establishTasksHologram(player);
                        HologramLayer.getInstance().setTasks(player.getUniqueId().toString(), membersTasks);
                    });
                } catch (SQLException | DataAccessException e) {
                    e.printStackTrace();
                }
            });
        }
        return true;
    }
}
