package me.colormaestro.taskmanager;

import me.colormaestro.taskmanager.data.DataAccessException;
import me.colormaestro.taskmanager.data.PlayerDAO;
import me.colormaestro.taskmanager.data.TaskDAO;
import me.colormaestro.taskmanager.model.Task;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.List;

public class Tasks implements CommandExecutor {
    private final TaskDAO taskDAO;
    private final PlayerDAO playerDAO;

    public Tasks(TaskDAO taskDAO, PlayerDAO playerDAO) {
        this.taskDAO = taskDAO;
        this.playerDAO = playerDAO;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (args.length == 1 && args[0].equals("help")) {
            sendHelp(sender);
            return true;
        }

        if ((args.length == 0 && sender instanceof Player) || args.length == 1) {
            Player p = (Player) sender;
            try {
                int id = playerDAO.getPlayerID(p.getUniqueId());
                List<Task> tasks = taskDAO.fetchPlayersActiveTasks(id);
                sendTasks(p, tasks);
            } catch (SQLException | DataAccessException ex) {
                p.sendMessage(ChatColor.RED + ex.getMessage());
                ex.printStackTrace();
            }
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Usage: /tasks [player] or /tasks help");
        return true;
    }

    private void sendHelp(CommandSender sender) {
        ChatColor g = ChatColor.GOLD;
        ChatColor w = ChatColor.WHITE;
        sender.sendMessage(ChatColor.AQUA + "-=-=-=-=-=- TaskManager help -=-=-=-=-=-");
        sender.sendMessage(g + "/tasks help" + w + " - shows this help");
        sender.sendMessage(g + "/tasks [IGN]" + w + " - shows your or other player tasks");
        sender.sendMessage(g + "/visittask <id>" + w + " - teleports to the task workplace");
        sender.sendMessage(g + "/addtask <IGN> <description>" + w + " - adds new task");
        sender.sendMessage(g + "/finishtask <id>" + w + " - mark task as finished");
        sender.sendMessage(g + "/approvetask <id> [force]" + w + " - approves the finished task (with force also unfinished one)");
        sender.sendMessage(g + "/settaskplace <id>" + w + " - sets spawning point for this task for more comfort :)");
    }

    private void sendTasks(Player p, List<Task> tasks) {
        p.sendMessage(ChatColor.AQUA + "-=-=-=- " + p.getName() + "'s tasks -=-=-=-");
        for (Task task : tasks) {
            switch (task.getStatus()) {
                case DOING:
                    p.sendMessage(ChatColor.GOLD + task.getDescription());
                    break;
                case FINISHED:
                    p.sendMessage(ChatColor.GREEN + task.getDescription());
                    break;
            }
        }
    }
}
