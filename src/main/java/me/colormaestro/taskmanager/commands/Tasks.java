package me.colormaestro.taskmanager.commands;

import me.colormaestro.taskmanager.data.DataAccessException;
import me.colormaestro.taskmanager.data.MemberDAO;
import me.colormaestro.taskmanager.data.TaskDAO;
import me.colormaestro.taskmanager.model.AdvisedTask;
import me.colormaestro.taskmanager.model.BasicMemberInfo;
import me.colormaestro.taskmanager.model.IdleTask;
import me.colormaestro.taskmanager.model.Member;
import me.colormaestro.taskmanager.model.StringReporter;
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
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Tasks implements CommandExecutor {
    private final Plugin plugin;
    private final TaskDAO taskDAO;
    private final MemberDAO memberDAO;
    private final Random rand;

    private final String[][] HELP_PAGE_1 = {
            {"/tasks help [page]", "shows this help"},
            {"/dashboard", "shows tasks dashboard"},
            {"/dashboard <IGN>", "jumps directly in dashboard to selected member tasks"},
            {"/tasks supervised", "shows tasks, which you are advising"},
            {"/tasks stats", "shows task statistics"},
            {"/tasks prepared", "shows task which are prepared for members"},
            {"/tasks idle", "shows task on which members work too long"},
            {"/tasks [IGN]", "shows your or other member's tasks"}
    };

    private final String[][] HELP_PAGE_2 = {
            {"/visittask <id>", "teleports to the task workplace"},
            {"/taskinfo <id>", "obtains info in book for related task"},
            {"/needtasks [limit]", "shows members who have up to limit tasks in progress (default is 0)"},
            {"/addmember <IGN>", "adds player as member"},
            {"/removemember <IGN>", "removes player's membership"},
            {"/addtask <IGN>", "creates task assignment book with blank description"},
            {"/addtask <IGN> [id]", "creates task assignment book, description is taken from selected task"},
            {"/preparetask", "creates task book for creating of prepared task"},
    };

    private final String[][] HELP_PAGE_3 = {
            {"/assigntask <IGN> <id>", "assigns prepared tasks to member"},
            {"/finishtask <id>", "marks task as finished"},
            {"/approvetask <id> [force]", "approves the finished task"},
            {"/returntask <id> [force]", "returns task back to given (unfinished) state"},
            {"/transfertask <id> <IGN>", "changes the assignee of the task"},
            {"/settaskplace <id>", "sets spawning point for this task for more comfort :)"},
            {"/synchronizetasks", "synchronizes state of integrations with state of plugins database"},
            {"/linkdiscord", "links discord account for notifications"},
            {"/establish", "establishes the Hologram where is summary of member's tasks"}
    };

    public Tasks(Plugin plugin, TaskDAO taskDAO, MemberDAO memberDAO) {
        this.plugin = plugin;
        this.taskDAO = taskDAO;
        this.memberDAO = memberDAO;
        this.rand = new Random();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {

        if ((args.length == 1 || args.length == 2) && args[0].equals("help")) {
            if (args.length == 1) {
                sendHelp(sender, "1");
            } else {
                sendHelp(sender, args[1]);
            }
            return true;
        }

        if (!(sender instanceof Player player)) {
            return true;
        }

        if (args.length == 1 && args[0].equals("supervised")) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    Member member = memberDAO.findMember(player.getUniqueId());
                    List<AdvisedTask> tasks = taskDAO.fetchAdvisorActiveTasks(member.getId());
                    String[] messages = formatMessages(tasks, "No active supervised tasks", ChatColor.LIGHT_PURPLE + "-=-=-=- " + player.getName() + "'s supervised tasks -=-=-=-");
                    Bukkit.getScheduler().runTask(plugin, () -> player.sendMessage(messages));
                } catch (SQLException | DataAccessException ex) {
                    Bukkit.getScheduler().runTask(plugin, () -> player.sendMessage(ChatColor.RED + ex.getMessage()));
                    ex.printStackTrace();
                }
            });
            return true;
        }

        if (args.length == 1 && args[0].equals("stats")) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    List<BasicMemberInfo> stats = taskDAO.fetchMembersDashboardInfo();

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        ItemStack book = buildStatsBook(stats);
                        player.openBook(book);
                    });
                } catch (SQLException ex) {
                    Bukkit.getScheduler().runTask(plugin, () -> player.sendMessage(ChatColor.RED + ex.getMessage()));
                    ex.printStackTrace();
                }
            });
            return true;
        }

        if (args.length == 1 && args[0].equals("prepared")) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    List<Task> preparedTasks = taskDAO.fetchPreparedTasks();
                    String[] messages = formatMessages(preparedTasks, "No prepared tasks", ChatColor.GRAY + "-=-=-=- Prepared tasks -=-=-=-");

                    Bukkit.getScheduler().runTask(plugin, () -> player.sendMessage(messages));
                } catch (SQLException ex) {
                    Bukkit.getScheduler().runTask(plugin, () -> player.sendMessage(ChatColor.RED + ex.getMessage()));
                    ex.printStackTrace();
                }
            });
            return true;
        }

        if (args.length == 1 && args[0].equals("idle")) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    List<IdleTask> preparedTasks = taskDAO.fetchIdleTasks();
                    String[] messages = formatMessages(preparedTasks, "No idle tasks", ChatColor.DARK_AQUA + "-=-=-=- Idle tasks -=-=-=-");

                    Bukkit.getScheduler().runTask(plugin, () -> player.sendMessage(messages));
                } catch (SQLException ex) {
                    Bukkit.getScheduler().runTask(plugin, () -> player.sendMessage(ChatColor.RED + ex.getMessage()));
                    ex.printStackTrace();
                }
            });
            return true;
        }

        if (args.length == 0 || args.length == 1) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    Member member;
                    if (args.length == 0) {
                        member = memberDAO.findMember(player.getUniqueId());
                    } else {
                        member = memberDAO.findMember(args[0]);
                    }
                    List<Task> tasks = taskDAO.fetchPlayersActiveTasks(member.getId());
                    String[] messages = formatMessages(tasks, member.getIgn() + " has no tasks", ChatColor.AQUA + "-=-=-=- " + member.getIgn() + "'s tasks -=-=-=-");
                    Bukkit.getScheduler().runTask(plugin, () -> player.sendMessage(messages));
                } catch (SQLException | DataAccessException ex) {
                    Bukkit.getScheduler().runTask(plugin, () -> player.sendMessage(ChatColor.RED + ex.getMessage()));
                    ex.printStackTrace();
                }
            });
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Usage: /tasks [player] or /tasks help");
        return true;
    }

    private void sendHelp(CommandSender sender, String page) {
        String[][] helpItems;
        switch (page) {
            case "3" -> helpItems = HELP_PAGE_3;
            case "2" -> helpItems = HELP_PAGE_2;
            default -> {
                helpItems = HELP_PAGE_1;
                page = "1";  // Protection for displaying against non-integer values
            }
        }
        sender.sendMessage(ChatColor.AQUA + "-=-=-=-=-=- TaskManager " +
                plugin.getDescription().getVersion() + " (" + page + "/3) help -=-=-=-=-=-");
        for (var item : helpItems) {
            sender.sendMessage(ChatColor.GOLD + item[0] + ChatColor.WHITE + " - " + item[1]);
        }
    }

    private String[] formatMessages(List<? extends StringReporter> tasks, String emptyMessage, String headline) {
        if (tasks.isEmpty()) {
            return new String[]{ChatColor.GREEN + emptyMessage};
        }
        List<String> messages = new ArrayList<>();
        messages.add(headline);
        tasks.forEach(task -> messages.add(task.getReport()));
        return messages.toArray(new String[0]);
    }

    private ItemStack buildStatsBook(List<BasicMemberInfo> stats) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta) book.getItemMeta();
        // Generates random headline color in book
        char[] options = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e'};
        ChatColor color = ChatColor.getByChar(options[rand.nextInt(15)]);
        ComponentBuilder builder =
                new ComponentBuilder(color + "" + ChatColor.BOLD + "Holy grail of all members\n");
        int pageRows = 0;
        for (BasicMemberInfo data : stats) {
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
