package me.colormaestro.taskmanager.tabcompleters;

import me.colormaestro.taskmanager.data.PlayerDAO;

import java.sql.SQLException;
import java.util.ArrayList;

public class TasksTabCompleter extends MembersTabCompleter {

    public TasksTabCompleter(PlayerDAO playerDAO) {
        super(playerDAO);
    }

    /**
     * (Re)loads all player names from database. Useful when new member comes to the server, so completer can be invoked
     * to update list of names.
     */
    @Override
    public void reload() {
        try {
            names = playerDAO.getAllIGN();
            // adds 3 special targets (used in /tasks command)
            names.add("help");
            names.add("given");
            names.add("stats");
        } catch (SQLException e) {
            e.printStackTrace();
            names = new ArrayList<>();
        }
    }
}
