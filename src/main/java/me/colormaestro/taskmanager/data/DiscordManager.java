package me.colormaestro.taskmanager.data;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import javax.security.auth.login.LoginException;

public class DiscordManager {
    private static DiscordManager instance;
    private JDA api = null;

    private DiscordManager(String token) {
        try {
            api = JDABuilder.createDefault(token).build();
        } catch (LoginException e) {
            System.out.println("Cannot login Discord bot - LoginException");
            e.printStackTrace();
        }
    }

    public static void instantiate(String token) {
        if (instance == null) {
            instance = new DiscordManager(token);
        }
    }

    public static DiscordManager getInstance() {
        return instance;
    }

    public void taskCreated(long userID, String assigner) {
        if (api != null)
            api.retrieveUserById(userID).flatMap(x -> x.openPrivateChannel()
                    .flatMap(y -> y.sendMessage(assigner + "created new task for you!"))).queue();
    }

    public void taskFinished(long userID, String assignee, int taskID) {
        if (api != null)
            api.retrieveUserById(userID).flatMap(x -> x.openPrivateChannel()
                    .flatMap(y -> y.sendMessage(assignee + "has finished task" + taskID))).queue();
    }

    public void taskApproved(long userID, String assigner) {
        if (api != null)
            api.retrieveUserById(userID).flatMap(x -> x.openPrivateChannel()
                    .flatMap(y -> y.sendMessage(assigner + "has approved your task! Great Job!"))).queue();
    }
}
