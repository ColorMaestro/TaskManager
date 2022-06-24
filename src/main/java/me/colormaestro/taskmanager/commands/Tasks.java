package me.colormaestro.taskmanager.commands;

import me.colormaestro.taskmanager.data.DataAccessException;
import me.colormaestro.taskmanager.data.PlayerDAO;
import me.colormaestro.taskmanager.data.TaskDAO;
import me.colormaestro.taskmanager.model.MyPlayer;
import me.colormaestro.taskmanager.model.Task;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.Plugin;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class Tasks implements CommandExecutor {
    private final Plugin plugin;
    private final TaskDAO taskDAO;
    private final PlayerDAO playerDAO;
    private final Random rand;

    public Tasks(Plugin plugin, TaskDAO taskDAO, PlayerDAO playerDAO) {
        this.plugin = plugin;
        this.taskDAO = taskDAO;
        this.playerDAO = playerDAO;
        this.rand = new Random();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (args.length == 1 && args[0].equals("help")) {
            sendHelp(sender);
            return true;
        }

        if (sender instanceof Player && args.length == 1 && args[0].equals("given")) {
            Player p = (Player) sender;
            UUID uuid = p.getUniqueId();
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    int id = playerDAO.getPlayerID(uuid);
                    List<Task> tasks = taskDAO.fetchAdvisorActiveTasks(id);
                    Bukkit.getScheduler().runTask(plugin,
                            () -> sendAdvisorTasks(p, tasks));
                } catch (SQLException | DataAccessException ex) {
                    Bukkit.getScheduler().runTask(plugin,
                            () -> p.sendMessage(ChatColor.RED + ex.getMessage()));
                    ex.printStackTrace();
                }

            });
            return true;
        }

        if (sender instanceof Player && args.length == 1 && args[0].equals("stats")) {
            Player p = (Player) sender;
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    List<Task> tasks = taskDAO.fetchAllTasks();
                    Map<Integer, MyPlayer> players = playerDAO.fetchAllPlayers();
                    Map<Integer, int[]> results = new HashMap<>();  // creates stats structure
                    for (Integer playerID : players.keySet()) {  // initialize
                        results.put(playerID, new int[3]);
                    }
                    for (Task task : tasks) {  // make stats
                        int[] stats = results.get(task.getAssigneeID());
                        switch (task.getStatus()) {
                            case DOING:
                                stats[0] += 1;
                                break;
                            case FINISHED:
                                stats[1] += 1;
                            default:
                                stats[2] += 1;
                        }
                    }
                    Map<String, int[]> data = new HashMap<>();  // since task has only ID of person, we need to re-map
                    for (Integer playerID : players.keySet()) {
                        data.put(players.get(playerID).getIgn(), results.get(playerID));
                    }
                    Bukkit.getScheduler().runTask(plugin,
                            () -> {
                                ItemStack book = buildStatsBook(data);
                                p.openBook(book);
                            });
                } catch (SQLException ex) {
                    Bukkit.getScheduler().runTask(plugin,
                            () -> p.sendMessage(ChatColor.RED + ex.getMessage()));
                    ex.printStackTrace();
                }

            });
            return true;
        }

        if (sender instanceof Player && (args.length == 0 || args.length == 1)) {
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
        sender.sendMessage(g + "/tasks given" + w + " - shows tasks, which you are advising");
        sender.sendMessage(g + "/tasks stats" + w + " - shows task statistics");
        sender.sendMessage(g + "/tasks [IGN]" + w + " - shows your or other player tasks");
        sender.sendMessage(g + "/visittask <id>" + w + " - teleports to the task workplace");
        sender.sendMessage(g + "/taskinfo <id>" + w + " - obtains info in book for related task");
        sender.sendMessage(g + "/addtask <IGN>" + w + " - creates task assignment book with blank description");
        sender.sendMessage(g + "/addtask <IGN> [id]" + w + " - creates task assignment book, description is taken from selected task");
        sender.sendMessage(g + "/finishtask <id>" + w + " - marks task as finished");
        sender.sendMessage(g + "/approvetask <id> [force]" + w + " - approves the finished task");
        sender.sendMessage(g + "/returntask <id> [force]" + w + " - returns task back to given (unfinished) state");
        sender.sendMessage(g + "/transfertask <id> <IGN>" + w + " - changes the assignee of the task");
        sender.sendMessage(g + "/settaskplace <id>" + w + " - sets spawning point for this task for more comfort :)");
        sender.sendMessage(g + "/linkdiscord" + w + " - links discord account for notifications");
        sender.sendMessage(g + "/establish" + w + " - establishes the Hologram where is summary of players tasks.");
    }

    private void sendTasks(Player p, List<Task> tasks, String name) {
        if (tasks.isEmpty()) {
            p.sendMessage(ChatColor.GREEN + name + " has no tasks");
            return;
        }
        p.sendMessage(ChatColor.AQUA + "-=-=-=- " + name + "'s tasks -=-=-=-");
        for (Task task : tasks) {
            switch (task.getStatus()) {
                case DOING -> p.sendMessage(ChatColor.GOLD + "[" + task.getId() + "] " + ChatColor.WHITE + task.getTitle());
                case FINISHED -> p.sendMessage(ChatColor.GREEN + "[" + task.getId() + "] " + ChatColor.WHITE + task.getTitle());
            }
        }
    }

    private void sendAdvisorTasks(Player p, List<Task> tasks) {
        if (tasks.isEmpty()) {
            p.sendMessage(ChatColor.GREEN + "No active supervised tasks");
            return;
        }
        p.sendMessage(ChatColor.LIGHT_PURPLE + "-=-=-=- " + p.getName() + "'s supervised tasks -=-=-=-");
        for (Task task : tasks) {
            switch (task.getStatus()) {
                case DOING -> p.sendMessage(ChatColor.GOLD + "[" + task.getId() + "] " + ChatColor.WHITE + task.getTitle());
                case FINISHED -> p.sendMessage(ChatColor.GREEN + "[" + task.getId() + "] " + ChatColor.WHITE + task.getTitle());
            }
        }
    }

    private ItemStack buildStatsBook(Map<String, int[]> data) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta) book.getItemMeta();
        // Generates random headline color in book
        char[] options = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e'};
        ChatColor color = ChatColor.getByChar(options[rand.nextInt(15)]);
        ComponentBuilder builder =
                new ComponentBuilder(color + "" + ChatColor.BOLD + "Holy grail of all members\n");
        int pageRows = 0;
        for (Map.Entry<String, int[]> entry : data.entrySet()) {
            int[] stats = entry.getValue();
            String row = entry.getKey() + " " + ChatColor.GOLD + stats[0] +
                    " " + ChatColor.GREEN + stats[1] +
                    " " + ChatColor.AQUA + stats[2] + "\n";
            builder.append(row);
            pageRows += 1;
            if (pageRows == 12) {
                bookMeta.spigot().addPage(builder.create());
                builder = new ComponentBuilder();
                pageRows = 0;
            }
        }
        if (pageRows != 0) {  // remaining rows
            bookMeta.spigot().addPage(builder.create());
        }
        bookMeta.setTitle("blank");
        bookMeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Holy grail of all members");
        bookMeta.setAuthor("Task Manager");
        book.setItemMeta(bookMeta);
        return book;
    }
}
