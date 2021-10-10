package me.colormaestro.taskmanager.commands;

import me.colormaestro.taskmanager.data.DiscordManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LinkDiscord implements CommandExecutor {


    public LinkDiscord() {}

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can't be run from console.");
            return true;
        }

        Player p = (Player) sender;
        String code = DiscordManager.getInstance().generateCode(p.getUniqueId());
        p.sendMessage(ChatColor.BLUE + "Direct message this to Task Manager bot on discord:" +
                ChatColor.YELLOW + ChatColor.BOLD + " !code " + code);
        p.sendMessage(ChatColor.BLUE + "Be fast, code expires in 60 seconds!");
        return true;
    }
}
