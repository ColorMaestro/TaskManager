package me.colormaestro.taskmanager;

import me.colormaestro.taskmanager.data.DataAccessException;
import me.colormaestro.taskmanager.data.TaskDAO;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.*;

public class ApproveTask implements CommandExecutor {
    private TaskDAO taskDAO;

    public ApproveTask(TaskDAO taskDAO) {
        this.taskDAO = taskDAO;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command cant be run from console.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "You must specify task_id.");
            return true;
        }

        Player p = (Player) sender;
        boolean force = args.length == 2 && args[1].equals("force");
        try {
            int id = Integer.parseInt(args[0]);
            taskDAO.approveTask(id, force);
            p.sendMessage(ChatColor.GREEN + "Task approved.");
        } catch (SQLException | DataAccessException | NumberFormatException ex) {
            p.sendMessage(ChatColor.RED + ex.getMessage());
            ex.printStackTrace();
        }
        return true;
    }
}
