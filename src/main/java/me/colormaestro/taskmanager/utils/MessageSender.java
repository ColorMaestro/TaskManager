package me.colormaestro.taskmanager.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class MessageSender {

    /**
     * Sends message to online player with corresponding uuid.
     *
     * @param uuid of target player
     * @param message to send
     * @return true, if player was online and message was sent, false otherwise
     */
    public static boolean sendMessageIfOnline(String uuid, String message) {
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target.getUniqueId().toString().equals(uuid)) {
                target.sendMessage(message);
                return true;
            }
        }
        return false;
    }
}
