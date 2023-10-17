package me.colormaestro.taskmanager.commands;

import me.colormaestro.taskmanager.utils.ItemStackCreator;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class PrepareTask implements CommandExecutor {

    private final ItemStackCreator stackCreator;

    public PrepareTask(Plugin plugin) {
        this.stackCreator = new ItemStackCreator(plugin);
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(ChatColor.RED + "This command can't be run from console.");
            return true;
        }

        ItemStack book = stackCreator.createAssignmentBook(null, "");
        p.getInventory().addItem(book);

        return true;
    }
}
