package me.colormaestro.taskmanager.tabcompleters;

import me.colormaestro.taskmanager.data.MemberDAO;

import java.sql.SQLException;
import java.util.ArrayList;

public class TasksTabCompleter extends MembersTabCompleter {

    public TasksTabCompleter(MemberDAO memberDAO) {
        super(memberDAO);
    }

    /**
     * (Re)loads all player names from database. Useful when new member comes to the server, so completer can be invoked
     * to update list of names.
     */
    @Override
    public void reload() {
        try {
            names = memberDAO.getMembersNames();
            // adds 3 special targets (used in /tasks command)
            names.add("help");
            names.add("given");
            names.add("stats");
            names.add("prepared");
        } catch (SQLException e) {
            e.printStackTrace();
            names = new ArrayList<>();
        }
    }
}
