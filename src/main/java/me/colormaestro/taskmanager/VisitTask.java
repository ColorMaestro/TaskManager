package me.colormaestro.taskmanager;

import me.colormaestro.taskmanager.data.DataAccessException;
import me.colormaestro.taskmanager.data.TaskDAO;
import me.colormaestro.taskmanager.model.Task;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class VisitTask implements CommandExecutor {
    private final TaskDAO taskDAO;

    public VisitTask(TaskDAO taskDAO) {
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

        Player p = (Player) sender;
        try {
            int id = Integer.parseInt(args[0]);
            Task task = taskDAO.findTask(id);
            Location location = new Location(p.getWorld(), task.getX(), task.getY(), task.getZ(),
                    task.getYaw(), task.getPitch());
            p.teleport(location);
        } catch (SQLException | DataAccessException | NumberFormatException ex) {
            p.sendMessage(ChatColor.RED + ex.getMessage());
            ex.printStackTrace();
        }
        return true;
    }
}
