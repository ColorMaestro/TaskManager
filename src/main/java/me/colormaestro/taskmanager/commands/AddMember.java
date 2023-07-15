package me.colormaestro.taskmanager.commands;

import me.colormaestro.taskmanager.data.PlayerDAO;
import me.colormaestro.taskmanager.tabcompleters.AddTaskTabCompleter;
import me.colormaestro.taskmanager.tabcompleters.TasksTabCompleter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public class AddMember implements CommandExecutor {
    private final Plugin plugin;
    private final PlayerDAO playerDAO;
    private final TasksTabCompleter completer;
    private final AddTaskTabCompleter completerA;

    public AddMember(Plugin plugin, PlayerDAO playerDAO, TasksTabCompleter completer, AddTaskTabCompleter completerA) {
        this.plugin = plugin;
        this.playerDAO = playerDAO;
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
        String uuid = null;
        for (OfflinePlayer p : Bukkit.getServer().getOfflinePlayers()) {
            if (p.getName().equals(ign)) {
                uuid = p.getUniqueId().toString();
            }
        }

        if (uuid == null) {
            player.sendMessage(ChatColor.RED + "Player " + ign + " has never been here! Make sure he joined at least once.");
            return true;
        }

        String finalUuid = uuid;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (!playerDAO.playerExists(finalUuid)) {
                    playerDAO.addPlayer(finalUuid, ign);
                    completer.reload();
                    completerA.reload();
                    Bukkit.getScheduler().runTask(plugin, () -> player.sendMessage(
                            ChatColor.GREEN + "Player " + ign + " was added as member."));
                } else {
                    Bukkit.getScheduler().runTask(plugin, () -> player.sendMessage(
                            ChatColor.RED + "Player " + ign + " has already been added as member!"));
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        return true;
    }
}