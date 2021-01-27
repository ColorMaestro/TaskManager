package me.colormaestro.taskmanager.listeners;

import me.colormaestro.taskmanager.data.DataAccessException;
import me.colormaestro.taskmanager.data.HologramLayer;
import me.colormaestro.taskmanager.data.PlayerDAO;
import me.colormaestro.taskmanager.data.TaskDAO;
import me.colormaestro.taskmanager.enums.TaskStatus;
import me.colormaestro.taskmanager.model.Task;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import java.sql.Date;
import java.sql.SQLException;
import java.util.UUID;
import java.util.regex.PatternSyntaxException;

public class CustomListener implements Listener {
    private final TaskDAO taskDAO;
    private final PlayerDAO playerDAO;

    public CustomListener(TaskDAO taskDAO, PlayerDAO playerDAO) {
        this.taskDAO = taskDAO;
        this.playerDAO = playerDAO;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("TaskManager"),
                function(event),
                200);
    }

    private static Runnable function(PlayerJoinEvent event) {
        return () -> {
            String uuid = event.getPlayer().getUniqueId().toString();
            if (!HologramLayer.getInstance().hologramExists(uuid)) {
                event.getPlayer().sendMessage(ChatColor.DARK_AQUA +
                        "⚠ Your visual task list has not been established yet");
                event.getPlayer().sendMessage(ChatColor.DARK_AQUA +
                        "⚠ To do so issue command" + ChatColor.GOLD + "" + ChatColor.BOLD +
                        " /establish" + ChatColor.DARK_AQUA +" on the place, where you want to have it");
            }
        };
    }

    @EventHandler
    public void onBookEdit(PlayerEditBookEvent event) {
        if (!event.getNewBookMeta().hasAuthor()) {
            return;
        }

        String page = event.getNewBookMeta().getPage(1);
        if (!page.startsWith("*@create")) {
            return;
        }

        Plugin plugin = Bukkit.getPluginManager().getPlugin("TaskManager");
        Player p = event.getPlayer();
        UUID uuid = p.getUniqueId();

        String prevDispName = event.getPreviousBookMeta().getDisplayName();
        String[] tmp = prevDispName.split(":");
        if (tmp.length != 2) {
            p.sendMessage(ChatColor.DARK_PURPLE + "Ayyy you, you found the plugin secret :D POG, however, you need "
                    + "to use /addtask command for creating tasks ;)");
            return;
        }
        String ign = tmp[1];

        String description = event.getNewBookMeta().getPage(2);
        String title = event.getNewBookMeta().getTitle();

        double x = p.getLocation().getX();
        double y = p.getLocation().getY();
        double z = p.getLocation().getZ();
        float yaw = p.getLocation().getYaw();
        float pitch = p.getLocation().getPitch();

        Bukkit.getScheduler().runTaskAsynchronously(plugin,
                () -> {
                    int assigneeID = 0, advisorID = 0;
                    try {
                        assigneeID = playerDAO.getPlayerID(ign);
                        advisorID = playerDAO.getPlayerID(uuid);
                    } catch (SQLException | DataAccessException ex) {
                        Bukkit.getScheduler().runTask(plugin,
                                () -> p.sendMessage(ChatColor.RED + ex.getMessage()));
                        ex.printStackTrace();
                    }
                    Task task = new Task(title, description, assigneeID, advisorID, x, y, z, yaw, pitch,
                            TaskStatus.DOING, new Date(System.currentTimeMillis()), null);
                    try {
                        taskDAO.createTask(task);
                        Bukkit.getScheduler().runTask(plugin,
                                () -> p.sendMessage(ChatColor.GREEN + "Task added."));
                    } catch (SQLException | IllegalArgumentException ex) {
                        Bukkit.getScheduler().runTask(plugin,
                                () -> p.sendMessage(ChatColor.RED + ex.getMessage()));
                        ex.printStackTrace();
                    }
                });
    }
}
