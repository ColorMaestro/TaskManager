package me.colormaestro.taskmanager.commands;

import me.colormaestro.taskmanager.scheduler.Scheduler;
import me.colormaestro.taskmanager.utils.RunnablesCreator;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Dashboard implements CommandExecutor {
    private final Scheduler scheduler;
    private final RunnablesCreator creator;

    public Dashboard(Scheduler scheduler, RunnablesCreator creator) {
        this.scheduler = scheduler;
        this.creator = creator;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can't be run from console.");
            return true;
        }

        if (args.length > 0) {
            scheduler.runTaskAsynchronously(creator.showActiveTasksView(player, args[0], 1));
            return true;
        }

        scheduler.runTaskAsynchronously(creator.showDashboardView(player, 1));

        return true;
    }
}
