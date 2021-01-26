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
import org.bukkit.plugin.Plugin;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

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

        if (sender instanceof Player && (args.length == 0 || args.length == 1)) {
            Plugin plugin = Bukkit.getPluginManager().getPlugin("TaskManager");
            Player p = (Player) sender;
            UUID uuid = p.getUniqueId();
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    int id;
                    if (args.length == 0) {
                        id = playerDAO.getPlayerID(uuid);
                    } else {
                        id = playerDAO.getPlayerID(args[0]);
                    }
                    String playerIGN = playerDAO.getPlayerIGN(id);
                    List<Task> tasks = taskDAO.fetchPlayersActiveTasks(id);
                    Bukkit.getScheduler().runTask(plugin,
                            () -> sendTasks(p, tasks, playerIGN));
                } catch (SQLException | DataAccessException ex) {
                    Bukkit.getScheduler().runTask(plugin,
                            () -> p.sendMessage(ChatColor.RED + ex.getMessage()));
                    ex.printStackTrace();
                }
            });
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

    private void sendTasks(Player p, List<Task> tasks, String name) {
        if (tasks.isEmpty()) {
            p.sendMessage(ChatColor.GREEN + name + " has no tasks");
            return;
        }
        p.sendMessage(ChatColor.AQUA + "-=-=-=- " + name + "'s tasks -=-=-=-");
        for (Task task : tasks) {
            switch (task.getStatus()) {
                case DOING:
                    p.sendMessage(ChatColor.GOLD + "[" + task.getId() + "] " + ChatColor.WHITE + task.getDescription());
                    break;
                case FINISHED:
                    p.sendMessage(ChatColor.GREEN + "[" + task.getId() + "] " + ChatColor.WHITE + task.getDescription());
                    break;
            }
        }
    }
}
