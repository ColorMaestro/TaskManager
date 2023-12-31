package me.colormaestro.taskmanager.commands;

import me.colormaestro.taskmanager.utils.RunnablesCreator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class AssignTask implements CommandExecutor {

    private final RunnablesCreator creator;

    public AssignTask(RunnablesCreator creator) {
        this.creator = creator;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can't be run from console.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "You must specify member and task.");
            return true;
        }

        try {
            Plugin plugin = creator.getPlugin();
            String ign = args[0];
            int taskId = Integer.parseInt(args[1]);
            Bukkit.getScheduler().runTaskAsynchronously(plugin, creator.assignTask(ign, player, taskId));
        } catch (NumberFormatException ex) {
            Bukkit.getScheduler().runTask(creator.getPlugin(),
                    () -> player.sendMessage(ChatColor.RED + "Task ID must be numerical value!"));
        }

        return true;
    }
}
