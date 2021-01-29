package me.colormaestro.taskmanager;

import me.colormaestro.taskmanager.data.PlayerDAO;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TasksTabCompleter implements TabCompleter {
    private final List<String> igns;

    public TasksTabCompleter(PlayerDAO playerDAO) {
        List<String> tmp;
        try {
            tmp = playerDAO.getAllIGN();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            tmp = new ArrayList<>();
        }
        this.igns = tmp;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (sender instanceof Player) {
            if (args.length == 1) {
                List<String> results = new ArrayList<>();
                for (String ign : igns) {
                    if (ign.toLowerCase().contains(args[0].toLowerCase())) {
                        results.add(ign);
                    }
                }
                if ("help".contains(args[0].toLowerCase())) {
                    results.add("help");
                }
                return results;
            }
        }
        return null;
    }
}
