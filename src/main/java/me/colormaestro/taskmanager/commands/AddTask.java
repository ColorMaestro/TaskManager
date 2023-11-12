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
    private final ItemStackCreator stackCreator;

    public AddTask(Plugin plugin, TaskDAO taskDAO) {
        this.plugin = plugin;
        this.taskDAO = taskDAO;
        this.stackCreator = new ItemStackCreator(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can't be run from console.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "You must provide player name who to add task to.");
            return true;
        }

        String ign = args[0];
        if (args.length == 1) {  // Empty description
            ItemStack book = stackCreator.createAssignmentBook(ign, "");
            player.getInventory().addItem(book);
        } else {  // Description taken from selected task
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    int taskId = Integer.parseInt(args[1]);
                    Task task = taskDAO.findTask(taskId);
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        ItemStack book = stackCreator.createAssignmentBook(ign, task.getDescription());
                        player.getInventory().addItem(book);
                    });
                } catch (SQLException | DataAccessException | NumberFormatException ex) {
                    Bukkit.getScheduler().runTask(plugin,
                            () -> player.sendMessage(ChatColor.RED + ex.getMessage()));
                    ex.printStackTrace();
                }
            });
        }
        return true;
    }
}
