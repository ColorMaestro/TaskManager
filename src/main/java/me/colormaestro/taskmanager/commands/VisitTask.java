package me.colormaestro.taskmanager.commands;

import me.colormaestro.taskmanager.data.DataAccessException;
import me.colormaestro.taskmanager.data.PlayerDAO;
import me.colormaestro.taskmanager.data.TaskDAO;
import me.colormaestro.taskmanager.model.Task;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.Plugin;

import java.sql.SQLException;

public class VisitTask implements CommandExecutor {
    private final TaskDAO taskDAO;
    private final PlayerDAO playerDAO;

    public VisitTask(TaskDAO taskDAO, PlayerDAO playerDAO) {
        this.taskDAO = taskDAO;
        this.playerDAO = playerDAO;
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

        Plugin plugin = Bukkit.getPluginManager().getPlugin("TaskManager");
        Player p = (Player) sender;
        String sid = args[0];
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                int id = Integer.parseInt(sid);
                Task task = taskDAO.findTask(id);
                String advisorName = playerDAO.getPlayerIGN(task.getAdvisorID());
                Bukkit.getScheduler().runTask(plugin,
                        () -> {
                            Location location = new Location(p.getWorld(), task.getX(), task.getY(), task.getZ(),
                                    task.getYaw(), task.getPitch());
                            ItemStack book = buildBook(task, advisorName);
                            p.teleport(location);
                            p.getInventory().addItem(book);
                        });
            } catch (SQLException | DataAccessException | NumberFormatException ex) {
                Bukkit.getScheduler().runTask(plugin,
                        () -> p.sendMessage(ChatColor.RED + ex.getMessage()));
                ex.printStackTrace();
            }
        });
        return true;
    }

    private ItemStack buildBook(Task task, String advisorName) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta) book.getItemMeta();

        BaseComponent[] page = new ComponentBuilder("   " + task.getTitle() + "\n")
                .color(net.md_5.bungee.api.ChatColor.BLUE).bold(true)
                .append("From: ")
                .color(net.md_5.bungee.api.ChatColor.RESET).bold(false)
                .append(advisorName + "\n")
                .color(net.md_5.bungee.api.ChatColor.GOLD)
                .append("Created: " + task.getDateCreation() + "\n\n")
                .color(net.md_5.bungee.api.ChatColor.RESET)
                .append("Turn the page for instructions!\n")
                .create();

        BaseComponent[] page2 = new ComponentBuilder(task.getDescription()).create();

        bookMeta.spigot().addPage(page);
        bookMeta.spigot().addPage(page2);
        bookMeta.setTitle("blank");
        bookMeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Task " + task.getId());
        bookMeta.setAuthor(advisorName);
        book.setItemMeta(bookMeta);
        return book;
    }
}
