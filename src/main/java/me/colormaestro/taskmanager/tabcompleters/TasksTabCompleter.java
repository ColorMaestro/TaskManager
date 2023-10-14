package me.colormaestro.taskmanager.tabcompleters;

import me.colormaestro.taskmanager.data.MemberDAO;

import java.sql.SQLException;
import java.util.ArrayList;

public class TasksTabCompleter extends MembersTabCompleter {

    public TasksTabCompleter(MemberDAO memberDAO) {
        super(memberDAO);
    }

    @Override
    public void reload() {
        try {
            names = memberDAO.getMembersNames();
            names.add("help");
            names.add("given");
            names.add("stats");
            names.add("prepared");
            names.add("idle");
        } catch (SQLException e) {
            e.printStackTrace();
            names = new ArrayList<>();
        }
    }
}
