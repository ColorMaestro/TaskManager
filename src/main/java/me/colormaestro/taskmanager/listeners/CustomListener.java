package me.colormaestro.taskmanager.listeners;

import me.colormaestro.taskmanager.data.DataAccessException;
import me.colormaestro.taskmanager.data.DiscordManager;
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
import java.util.List;
import java.util.UUID;

public class CustomListener implements Listener {
    private final Plugin plugin;
    private final TaskDAO taskDAO;
    private final PlayerDAO playerDAO;

    public CustomListener(Plugin plugin, TaskDAO taskDAO, PlayerDAO playerDAO) {
        this.plugin = plugin;
        this.taskDAO = taskDAO;
        this.playerDAO = playerDAO;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, addPlayerToDB(event, plugin, playerDAO), 200);
        Bukkit.getScheduler().runTaskLater(plugin, checkHologram(event), 250);
    }

    private static Runnable checkHologram(PlayerJoinEvent event) {
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

    private static Runnable addPlayerToDB(PlayerJoinEvent event, Plugin plugin, PlayerDAO playerDAO) {
        return () -> {
            String uuid = event.getPlayer().getUniqueId().toString();
            String ign = event.getPlayer().getName();
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    if (!playerDAO.playerExists(uuid)) {
                        playerDAO.addPlayer(uuid, ign);
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            Bukkit.getServer().broadcastMessage(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Player " +
                                    ign + " has been added to database (first join)");
                        });
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            });
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
                        List<Task> activeTasks = taskDAO.fetchPlayersActiveTasks(assigneeID);
                        String assigneeUUID = playerDAO.getPlayerUUID(assigneeID);
                        long discordUserID = playerDAO.getDiscordUserID(assigneeUUID);
                        Bukkit.getScheduler().runTask(plugin,
                                () -> {
                                    p.sendMessage(ChatColor.GREEN + "Task added.");

                                    // Firstly we try to notify the assignee in game
                                    boolean messageSent = false;
                                    HologramLayer.getInstance().setTasks(assigneeUUID, activeTasks);
                                    for (Player target : Bukkit.getOnlinePlayers()) {
                                        if (target.getUniqueId().toString().equals(assigneeUUID)) {
                                            target.sendMessage(ChatColor.GOLD + "You have new task from " + p.getName());
                                            target.playSound(target.getLocation(),
                                                    "minecraft:record.newtask", 10, 1);
                                            messageSent = true;
                                            break;
                                        }
                                    }

                                    // If the assignee is not online, sent him message to discord
                                    if (!messageSent) {
                                        DiscordManager.getInstance().taskCreated(discordUserID, p.getName());
                                    }
                                });
                    } catch (SQLException | IllegalArgumentException | DataAccessException ex) {
                        Bukkit.getScheduler().runTask(plugin,
                                () -> p.sendMessage(ChatColor.RED + ex.getMessage()));
                        ex.printStackTrace();
                    }
                });
    }
}
