package me.colormaestro.taskmanager.commands;

import me.colormaestro.taskmanager.data.DataAccessException;
import me.colormaestro.taskmanager.data.PlayerDAO;
import me.colormaestro.taskmanager.data.TaskDAO;
import me.colormaestro.taskmanager.model.Task;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.sql.SQLException;

public class TaskInfo implements CommandExecutor {
    private final TaskDAO taskDAO;
    private final PlayerDAO playerDAO;

    public TaskInfo(TaskDAO taskDAO, PlayerDAO playerDAO) {
        this.taskDAO = taskDAO;
        this.playerDAO = playerDAO;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
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
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                int id = Integer.parseInt(sid);
                Task task = taskDAO.findTask(id);
                String advisorName = playerDAO.getPlayerIGN(task.getAdvisorID());
                String assigneeName = playerDAO.getPlayerIGN(task.getAssigneeID());
                Bukkit.getScheduler().runTask(plugin,
                        () -> {
                            ItemStack book = VisitTask.buildBook(task, advisorName, assigneeName);
                            p.getInventory().addItem(book);
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
