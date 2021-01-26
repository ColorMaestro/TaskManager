package me.colormaestro.taskmanager.commands;

import me.colormaestro.taskmanager.data.DataAccessException;
import me.colormaestro.taskmanager.data.TaskDAO;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.sql.SQLException;
import java.util.function.BiConsumer;

public class ApproveTask implements CommandExecutor {
    private final TaskDAO taskDAO;

    public ApproveTask(TaskDAO taskDAO) {
        this.taskDAO = taskDAO;
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
                        Bukkit.getScheduler().runTask(plugin,
                                () -> p.sendMessage(ChatColor.GREEN + "Task approved."));
                    } catch (SQLException | DataAccessException | NumberFormatException ex) {
                        Bukkit.getScheduler().runTask(plugin,
                                () -> p.sendMessage(ChatColor.RED + ex.getMessage()));
                        ex.printStackTrace();
                    }
                });
        return true;
    }
}
