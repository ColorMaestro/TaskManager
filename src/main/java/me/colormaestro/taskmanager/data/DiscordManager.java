package me.colormaestro.taskmanager.data;

import me.colormaestro.taskmanager.model.Task;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import javax.security.auth.login.LoginException;
import java.awt.Color;
import java.time.Instant;

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

    public void taskCreated(long userID, String assigner, Task task) {
        if (api != null) {
            MessageEmbed e = new EmbedBuilder().setTitle(task.getTitle()).setColor(new Color(255, 132, 0))
                    .setDescription(task.getDescription()).setFooter(assigner).setTimestamp(Instant.now()).build();
            String message = ":small_orange_diamond: " + assigner + " created new task for you :small_orange_diamond:";
            api.retrieveUserById(userID).flatMap(x -> x.openPrivateChannel()
                    .flatMap(channel -> channel.sendMessage(message).embed(e))).queue();
        }
    }

    public void taskFinished(long userID, String assignee, Task task) {
        if (api != null) {
            String message = String.format(":bellhop: %s finished task *%s* (%d)", assignee, task.getTitle(), task.getId());
            api.retrieveUserById(userID).flatMap(x -> x.openPrivateChannel()
                    .flatMap(channel -> channel.sendMessage(message))).queue();
        }
    }

    public void taskApproved(long userID, String assigner, Task task) {
        if (api != null) {
            String message = String.format(":white_check_mark: %s approved your task *%s* (%d)", assigner, task.getTitle(), task.getId());
            api.retrieveUserById(userID).flatMap(x -> x.openPrivateChannel()
                    .flatMap(channel -> channel.sendMessage(message))).queue();
        }
    }

    public void taskTransfered(long userID, String advisor, String oldAssignee, String newAssignee, Task task, boolean taken) {
        if (api != null) {
            String messageGiven = String.format(":inbox_tray: %s has transferred task [%d] *%s* from %s to you.", advisor, task.getId(), task.getTitle(), oldAssignee);
            String messageTaken = String.format(":outbox_tray: %s has transferred your task [%d] *%s* to %s.", advisor, task.getId(), task.getTitle(), newAssignee);
            api.retrieveUserById(userID).flatMap(x -> x.openPrivateChannel()
                    .flatMap(channel -> channel.sendMessage(taken ? messageTaken : messageGiven))).queue();
        }
    }
}
