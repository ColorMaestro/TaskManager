package me.colormaestro.taskmanager;

import me.colormaestro.taskmanager.data.DataAccessException;
import me.colormaestro.taskmanager.data.PlayerDAO;
import me.colormaestro.taskmanager.data.TaskDAO;
import me.colormaestro.taskmanager.enums.TaskStatus;
import me.colormaestro.taskmanager.model.Task;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Date;
import java.sql.SQLException;


public class AddTask implements CommandExecutor {
    private final TaskDAO taskDAO;
    private final PlayerDAO playerDAO;

    public AddTask(TaskDAO taskDAO, PlayerDAO playerDAO) {
        this.taskDAO = taskDAO;
        this.playerDAO = playerDAO;
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
        int assigneeID = 0, advisorID = 0;
        try {
            assigneeID = playerDAO.getPlayerID(args[0]);
            advisorID = playerDAO.getPlayerID(p.getUniqueId());
        } catch (SQLException | DataAccessException ex) {
            p.sendMessage(ChatColor.RED + ex.getMessage());
            ex.printStackTrace();
        }
        String description = buildDescription(args);
        Task task = new Task(description, assigneeID, advisorID,
                p.getLocation().getX(),
                p.getLocation().getY(),
                p.getLocation().getZ(),
                p.getLocation().getYaw(),
                p.getLocation().getPitch(),
                TaskStatus.DOING,
                new Date(System.currentTimeMillis()),
                null
        );
        try {
            taskDAO.createTask(task);
            p.sendMessage(ChatColor.GREEN + "Task added.");
        } catch (SQLException | IllegalArgumentException ex) {
            p.sendMessage(ChatColor.RED + ex.getMessage());
            ex.printStackTrace();
        }
        return true;
    }

    private String buildDescription(String[] args) {
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            builder.append(args[i]).append(" ");
        }
        return builder.substring(0, builder.length() - 1);
    }
}
