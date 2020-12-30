package me.colormaestro.taskmanager;

import me.colormaestro.taskmanager.data.TaskDAO;
import me.colormaestro.taskmanager.model.Task;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.sql.*;

public class AddTask implements CommandExecutor {
    private TaskDAO taskDAO;

    public AddTask(TaskDAO taskDAO) {
        this.taskDAO = taskDAO;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command cant be run from console.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "You must provide description for task.");
            return true;
        }

        Player p = (Player) sender;
        Task task = new Task();
        try {
            taskDAO.createTask(task);
            p.sendMessage(ChatColor.GREEN + "Task added.");
        } catch (SQLException | IllegalArgumentException ex) {
            p.sendMessage(ChatColor.RED + ex.getMessage());
            ex.printStackTrace();
        }
        return true;
    }
}
