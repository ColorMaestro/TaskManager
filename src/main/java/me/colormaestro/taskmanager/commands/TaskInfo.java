package me.colormaestro.taskmanager.commands;

import me.colormaestro.taskmanager.utils.RunnablesCreator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TaskInfo implements CommandExecutor {
    private final RunnablesCreator creator;

    public TaskInfo(RunnablesCreator creator) {
        this.creator = creator;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can't be run from console.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "You must specify task id.");
            return true;
        }

        int taskId = Integer.parseInt(args[0]);
        Bukkit.getScheduler().runTaskAsynchronously(creator.getPlugin(), creator.givePlayerAssignmentBook(player, taskId));
        return true;
    }
}
