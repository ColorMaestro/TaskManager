package me.colormaestro.taskmanager;

import me.colormaestro.taskmanager.data.DataAccessException;
import me.colormaestro.taskmanager.data.PlayerDAO;
import me.colormaestro.taskmanager.data.TaskDAO;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.*;

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
            sender.sendMessage(ChatColor.RED + "This command cant be run from console.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "You must specify task id.");
            return true;
        }

        Player p = (Player) sender;
        try {
            int assignee = playerDAO.getPlayerID(p.getUniqueId());
            int id = Integer.parseInt(args[0]);
            taskDAO.finishTask(id, assignee);
            p.sendMessage(ChatColor.GREEN + "Task finished.");
        } catch (SQLException | DataAccessException | NumberFormatException ex) {
            p.sendMessage(ChatColor.RED + ex.getMessage());
            System.out.println(ex.getMessage());
        }
        return true;
    }
}
