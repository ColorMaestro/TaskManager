package me.colormaestro.taskmanager.tabcompleters;

import me.colormaestro.taskmanager.data.PlayerDAO;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AddTaskTabCompleter implements TabCompleter {
    protected final PlayerDAO playerDAO;
    protected List<String> names;

    public AddTaskTabCompleter(PlayerDAO playerDAO) {
        this.playerDAO = playerDAO;
        reload();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (sender instanceof Player && args.length == 1) {
            return names.stream()
                    .filter(ign -> ign.toLowerCase().contains(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return null;
    }

    /**
     * (Re)loads all player names from database. Useful when new member comes to the server, so completer can be invoked
     * to update list of names.
     */
    public void reload() {
        try {
            names = playerDAO.getAllIGN();
        } catch (SQLException e) {
            e.printStackTrace();
            names = new ArrayList<>();
        }
    }
}
