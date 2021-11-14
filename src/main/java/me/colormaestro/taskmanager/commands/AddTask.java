package me.colormaestro.taskmanager.commands;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.Arrays;


public class AddTask implements CommandExecutor {

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
        ItemStack book = buildBook(args[0]);
        p.getInventory().addItem(book);
        return true;
    }

    private ItemStack buildBook(String ign) {
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

        BaseComponent[] page2 = new ComponentBuilder("").create();

        bookMeta.spigot().addPage(page);
        bookMeta.spigot().addPage(page2);
        bookMeta.setDisplayName(ChatColor.GOLD + "Assignment book for " + ign);
        bookMeta.setLore(new ArrayList<>(Arrays.asList("*@create", ign)));
        book.setItemMeta(bookMeta);
        return book;
    }
}
