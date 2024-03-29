package me.colormaestro.taskmanager.integrations;

import me.colormaestro.taskmanager.data.DataAccessException;
import me.colormaestro.taskmanager.data.MemberDAO;
import me.colormaestro.taskmanager.listeners.DiscordMessageListener;
import me.colormaestro.taskmanager.model.Task;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import javax.security.auth.login.LoginException;
import java.awt.Color;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class DiscordOperator {
    private final MemberDAO memberDAO;
    private static DiscordOperator instance;
    private final JavaPlugin plugin;
    private final Map<String, UUID> codes;
    private JDA api = null;

    private DiscordOperator(String token, MemberDAO memberDAO, JavaPlugin plugin) {
        try {
            api = JDABuilder.createDefault(token).addEventListeners(new DiscordMessageListener()).build();
            plugin.getLogger().info("Token provided, bot connection with Discord established");
        } catch (LoginException | ErrorResponseException e) {
            plugin.getLogger().info("Cannot login Discord bot: " + e.getMessage());
        }
        codes = Collections.synchronizedMap(new HashMap<>());
        this.memberDAO = memberDAO;
        this.plugin = plugin;
    }

    public static void instantiate(String token, MemberDAO memberDAO, JavaPlugin plugin) {
        if (instance == null) {
            instance = new DiscordOperator(token, memberDAO, plugin);
        }
    }

    public static DiscordOperator getInstance() {
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
        String message = String.format(":bellhop: %s finished task [%d] *%s*", assignee, task.getId(), task.getTitle());
        sendMessage(userID, message);
    }

    public void taskApproved(long userID, String assigner, Task task) {
        String message = String.format(":white_check_mark: %s approved your task [%d] *%s*", assigner, task.getId(), task.getTitle());
        sendMessage(userID, message);
    }

    public void taskReturned(long userID, String assigner, Task task) {
        String message = String.format(":leftwards_arrow_with_hook: %s has returned your task [%d] *%s*", assigner, task.getId(), task.getTitle());
        sendMessage(userID, message);
    }

    public void taskTransferred(long userID, String advisor, String oldAssignee, String newAssignee, Task task, boolean taken) {
        String messageGiven = String.format(":inbox_tray: %s has transferred task [%d] *%s* from %s to you.", advisor, task.getId(), task.getTitle(), oldAssignee);
        String messageTaken = String.format(":outbox_tray: %s has transferred your task [%d] *%s* to %s.", advisor, task.getId(), task.getTitle(), newAssignee);
        sendMessage(userID, taken ? messageTaken : messageGiven);
    }

    private void sendMessage(long userID, String message) {
        if (api != null) {
            api.retrieveUserById(userID).flatMap(x -> x.openPrivateChannel()
                    .flatMap(channel -> channel.sendMessage(message))).queue();
        }
    }

    /**
     * Shuts down the bot instance.
     */
    public void shutdown() {
        if (api != null)
            api.shutdownNow();
    }

    /**
     * Generates authentication code for given UUID and stores it internally for short time.
     *
     * @param uuid of the player to authenticate
     * @return generated code
     */
    public String generateCode(UUID uuid) {
        StringBuilder builder = new StringBuilder();
        Random random = new Random();
        int i = 0;
        while (i < 10) {
            int number = 48 + random.nextInt(75);
            if ((58 <= number && number <= 64) || (91 <= number && number <= 96)) {
                continue;
            }
            builder.append((char) number);
            i++;
        }
        String code = builder.toString();
        synchronized (codes) {
            codes.put(code, uuid);
        }
        // Code automatically expires in 60 seconds
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            synchronized (codes) {
                codes.remove(code);
            }
        }, 1200);
        return code;
    }

    /**
     * Verifies, whether given code is present for user authentication. In case it is, the code is removed and
     * discord ID in the record with corresponding UUID is updated in database.
     *
     * @param code      code to verify
     * @param discordID discord user ID from received message
     * @return true, if code was present for user authentication, false otherwise
     */
    public boolean verifyCode(String code, long discordID) {
        synchronized (codes) {
            if (codes.containsKey(code)) {
                try {
                    memberDAO.setDiscordUserID(codes.get(code), discordID);
                    codes.remove(code);
                    return true;
                } catch (SQLException | DataAccessException ex) {
                    System.out.println(ex.getMessage());
                    ex.printStackTrace();
                    return false;
                }
            }
            return false;
        }
    }
}
