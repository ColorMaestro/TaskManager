package me.colormaestro.taskmanager.commands;

import me.colormaestro.taskmanager.data.DataAccessException;
import me.colormaestro.taskmanager.data.TaskDAO;
import me.colormaestro.taskmanager.model.Task;
import net.md_5.bungee.api.chat.BaseComponent;
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
import java.util.ArrayList;
import java.util.Arrays;


public class AddTask implements CommandExecutor {
    private final Plugin plugin;
    private final TaskDAO taskDAO;

    public AddTask(Plugin plugin, TaskDAO taskDAO) {
        this.plugin = plugin;
        this.taskDAO = taskDAO;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can't be run from console.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "You must provide player name who to add task to.");
            return true;
        }

        Player p = (Player) sender;
        String ign = args[0];
        if (args.length == 1) {  // Empty description
            ItemStack book = buildBook(ign, "");
            p.getInventory().addItem(book);
        } else {  // Description taken from selected task
            String sid = args[1];
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    int id = Integer.parseInt(sid);
                    Task task = taskDAO.findTask(id);
                    Bukkit.getScheduler().runTask(plugin,
                            () -> {
                                ItemStack book = buildBook(ign, task.getDescription());
                                p.getInventory().addItem(book);
                            });
                } catch (SQLException | DataAccessException | NumberFormatException ex) {
                    Bukkit.getScheduler().runTask(plugin,
                            () -> p.sendMessage(ChatColor.RED + ex.getMessage()));
                    ex.printStackTrace();
                }
            });
        }
        return true;
    }

    private ItemStack buildBook(String ign, String description) {
        ItemStack book = new ItemStack(Material.WRITABLE_BOOK);
        BookMeta bookMeta = (BookMeta) book.getItemMeta();

        BaseComponent[] page = new ComponentBuilder("Instructions:\n")
                .color(net.md_5.bungee.api.ChatColor.BLUE)
                .append("1) Only the second page of this book serves as task description " +
                    "for player what to do in this task.\n")
                .color(net.md_5.bungee.api.ChatColor.RESET)
                .append("2) Book title serves as headline for the task - this will be displayed at the hologram.\n")
                .append("3) Tasks is created immediately after you sign the book.\n")
                .create();

        BaseComponent[] page2 = new ComponentBuilder(description).create();

        bookMeta.spigot().addPage(page);
        bookMeta.spigot().addPage(page2);
        bookMeta.setDisplayName(ChatColor.GOLD + "Assignment book for " + ign);
        bookMeta.setLore(new ArrayList<>(Arrays.asList("*@create", ign)));
        book.setItemMeta(bookMeta);
        return book;
    }
}
