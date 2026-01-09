package me.colormaestro.taskmanager.commands;

import me.colormaestro.taskmanager.data.DataAccessException;
import me.colormaestro.taskmanager.data.MemberDAO;
import me.colormaestro.taskmanager.scheduler.Scheduler;
import me.colormaestro.taskmanager.tabcompleters.ReloadableTabCompleter;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public class RemoveMember implements CommandExecutor {
    private final Scheduler scheduler;
    private final MemberDAO memberDAO;
    private final ReloadableTabCompleter completer;
    private final ReloadableTabCompleter completerA;

    public RemoveMember(Scheduler scheduler, MemberDAO memberDAO, ReloadableTabCompleter completer, ReloadableTabCompleter completerA) {
        this.scheduler = scheduler;
        this.memberDAO = memberDAO;
        this.completer = completer;
        this.completerA = completerA;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can't be run from console.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "You must provide player name!");
            return true;
        }

        String ign = args[0];
        scheduler.runTaskAsynchronously(() -> {
            try {
                memberDAO.updateActivity(ign, false);
                completer.reload();
                completerA.reload();
                scheduler.runTask(() -> player.sendMessage(
                        ChatColor.GREEN + ign + " was removed as member. You can add them back later at any time." +
                                " Data about tasks are kept forever."));
            } catch (SQLException ex) {
                scheduler.runTask(() -> player.sendMessage(ChatColor.RED + ex.getMessage()));
                ex.printStackTrace();
            } catch (DataAccessException ex) {
                scheduler.runTask(() -> player.sendMessage(ChatColor.RED + ex.getMessage()));
            }
        });

        return true;
    }
}
