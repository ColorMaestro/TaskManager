package me.colormaestro.taskmanager.commands;

import me.colormaestro.taskmanager.data.DataAccessException;
import me.colormaestro.taskmanager.data.TaskDAO;
import me.colormaestro.taskmanager.model.Task;
import me.colormaestro.taskmanager.utils.ItemStackCreator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.sql.SQLException;


public class AddTask implements CommandExecutor {
    private final Plugin plugin;
    private final TaskDAO taskDAO;

    public AddTask(Plugin plugin, TaskDAO taskDAO) {
        this.plugin = plugin;
        this.taskDAO = taskDAO;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can't be run from console.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "You must provide player name who to add task to.");
            return true;
        }

        Player p = (Player) sender;
        String ign = args[0];
        if (args.length == 1) {  // Empty description
            ItemStack book = ItemStackCreator.createAssignmentBook(ign, "");
            p.getInventory().addItem(book);
        } else {  // Description taken from selected task
            String sid = args[1];
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    int id = Integer.parseInt(sid);
                    Task task = taskDAO.findTask(id);
                    Bukkit.getScheduler().runTask(plugin,
                            () -> {
                                ItemStack book = ItemStackCreator.createAssignmentBook(ign, task.getDescription());
                                p.getInventory().addItem(book);
                            });
                } catch (SQLException | DataAccessException | NumberFormatException ex) {
                    Bukkit.getScheduler().runTask(plugin,
                            () -> p.sendMessage(ChatColor.RED + ex.getMessage()));
                    ex.printStackTrace();
                }
            });
        }
        return true;
    }
}
