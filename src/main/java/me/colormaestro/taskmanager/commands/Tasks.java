package me.colormaestro.taskmanager.commands;

import me.colormaestro.taskmanager.data.DataAccessException;
import me.colormaestro.taskmanager.data.PlayerDAO;
import me.colormaestro.taskmanager.data.TaskDAO;
import me.colormaestro.taskmanager.model.AdvisedTask;
import me.colormaestro.taskmanager.model.MemberTaskStats;
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
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class Tasks implements CommandExecutor {
    private final Plugin plugin;
    private final TaskDAO taskDAO;
    private final PlayerDAO playerDAO;
    private final Random rand;

    private final String[][] commandsAndDescriptions = {
            {"/tasks help", "shows this help"},
            {"/addmember <IGN>", "adds player as member"},
            {"/dashboard", "shows tasks dashboard"},
            {"/dashboard <IGN>", "jumps directly in dashboard to selected member tasks"},
            {"/tasks given", "shows tasks, which you are advising"},
            {"/tasks stats", "shows task statistics"},
            {"/tasks prepared", "shows task which are prepared for members"},
            {"/tasks [IGN]", "shows your or other member's tasks"},
            {"/visittask <id>", "teleports to the task workplace"},
            {"/taskinfo <id>", "obtains info in book for related task"},
            {"/addtask <IGN>", "creates task assignment book with blank description"},
            {"/addtask <IGN> [id]", "creates task assignment book, description is taken from selected task"},
            {"/preparetask", "creates task book for creating of prepared task"},
            {"/assigntask <IGN> <id>", "assigns prepared tasks to member"},
            {"/finishtask <id>", "marks task as finished"},
            {"/approvetask <id> [force]", "approves the finished task"},
            {"/returntask <id> [force]", "returns task back to given (unfinished) state"},
            {"/transfertask <id> <IGN>", "changes the assignee of the task"},
            {"/settaskplace <id>", "sets spawning point for this task for more comfort :)"},
            {"/linkdiscord", "links discord account for notifications"},
            {"/establish", "establishes the Hologram where is summary of member's tasks"}
    };

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
                    List<AdvisedTask> tasks = taskDAO.fetchAdvisorActiveTasks(id);
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
                    List<MemberTaskStats> stats = taskDAO.fetchTaskStatistics();

                    Bukkit.getScheduler().runTask(plugin,
                            () -> {
                                ItemStack book = buildStatsBook(stats);
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

        if (sender instanceof Player && args.length == 1 && args[0].equals("prepared")) {
            Player p = (Player) sender;
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    List<Task> preparedTasks = taskDAO.fetchPreparedTasks();

                    Bukkit.getScheduler().runTask(plugin,
                            () -> sendPreparedTasks(p, preparedTasks));
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
        sender.sendMessage(ChatColor.AQUA + "-=-=-=-=-=- TaskManager help -=-=-=-=-=-");
        for (var item : commandsAndDescriptions) {
            sender.sendMessage(ChatColor.GOLD + item[0] + ChatColor.WHITE + " - " + item[1]);
        }
    }

    private void sendTasks(Player p, List<Task> tasks, String name) {
        if (tasks.isEmpty()) {
            p.sendMessage(ChatColor.GREEN + name + " has no tasks");
            return;
        }
        p.sendMessage(ChatColor.AQUA + "-=-=-=- " + name + "'s tasks -=-=-=-");
        for (Task task : tasks) {
            switch (task.getStatus()) {
                case DOING ->
                        p.sendMessage(ChatColor.GOLD + "[" + task.getId() + "] " + ChatColor.WHITE + task.getTitle());
                case FINISHED ->
                        p.sendMessage(ChatColor.GREEN + "[" + task.getId() + "] " + ChatColor.WHITE + task.getTitle());
            }
        }
    }

    private void sendAdvisorTasks(Player p, List<AdvisedTask> tasks) {
        if (tasks.isEmpty()) {
            p.sendMessage(ChatColor.GREEN + "No active supervised tasks");
            return;
        }
        p.sendMessage(ChatColor.LIGHT_PURPLE + "-=-=-=- " + p.getName() + "'s supervised tasks -=-=-=-");
        for (AdvisedTask task : tasks) {
            switch (task.status()) {
                case DOING -> p.sendMessage(ChatColor.GOLD + "[" + task.id() + "] " + ChatColor.WHITE + task.title() +
                        ChatColor.ITALIC + " (" + task.ign() + ")");
                case FINISHED ->
                        p.sendMessage(ChatColor.GREEN + "[" + task.id() + "] " + ChatColor.WHITE + task.title() +
                                ChatColor.ITALIC + " (" + task.ign() + ")");
            }
        }
    }

    private void sendPreparedTasks(Player p, List<Task> tasks) {
        if (tasks.isEmpty()) {
            p.sendMessage(ChatColor.GREEN + "No prepared tasks");
            return;
        }
        p.sendMessage(ChatColor.GRAY + "-=-=-=- Prepared tasks -=-=-=-");
        for (Task task : tasks) {
            p.sendMessage(ChatColor.GRAY + "[" + task.getId() + "] " + ChatColor.WHITE + task.getTitle());
        }
    }

    private ItemStack buildStatsBook(List<MemberTaskStats> stats) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta) book.getItemMeta();
        // Generates random headline color in book
        char[] options = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e'};
        ChatColor color = ChatColor.getByChar(options[rand.nextInt(15)]);
        ComponentBuilder builder =
                new ComponentBuilder(color + "" + ChatColor.BOLD + "Holy grail of all members\n");
        int pageRows = 0;
        for (MemberTaskStats data : stats) {
            String row = data.ign() + " " + ChatColor.GOLD + data.doing() +
                    " " + ChatColor.GREEN + data.finished() +
                    " " + ChatColor.AQUA + data.approved() + "\n";
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
