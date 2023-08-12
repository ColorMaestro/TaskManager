package me.colormaestro.taskmanager.listeners;

import me.colormaestro.taskmanager.data.DataAccessException;
import me.colormaestro.taskmanager.data.DiscordManager;
import me.colormaestro.taskmanager.data.HologramLayer;
import me.colormaestro.taskmanager.data.PlayerDAO;
import me.colormaestro.taskmanager.data.TaskDAO;
import me.colormaestro.taskmanager.enums.TaskStatus;
import me.colormaestro.taskmanager.model.Task;
import me.colormaestro.taskmanager.utils.Directives;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.plugin.Plugin;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class BookEditListener implements Listener {
    private final Plugin plugin;
    private final TaskDAO taskDAO;
    private final PlayerDAO playerDAO;

    public BookEditListener(Plugin plugin, TaskDAO taskDAO, PlayerDAO playerDAO) {
        this.plugin = plugin;
        this.taskDAO = taskDAO;
        this.playerDAO = playerDAO;
    }

    @EventHandler
    public void onBookEdit(PlayerEditBookEvent event) {
        if (!event.getNewBookMeta().hasAuthor()) {
            return;
        }

        Player p = event.getPlayer();
        UUID uuid = p.getUniqueId();

        List<String> lore = event.getPreviousBookMeta().getLore();
        String ign;
        if (lore == null || lore.size() != 2 || !lore.get(0).equals(Directives.CREATE_TASK)) {
            return;
        }
        ign = lore.get(1);

        if (event.getNewBookMeta().getPageCount() < 2) {
            p.sendMessage(ChatColor.GOLD + "Task description must not be empty! If you wish omit the description, put at least space char on second page");
            event.setCancelled(true);
            return;
        }

        String description = event.getNewBookMeta().getPage(2);
        String title = event.getNewBookMeta().getTitle();

        double x = p.getLocation().getX();
        double y = p.getLocation().getY();
        double z = p.getLocation().getZ();
        float yaw = p.getLocation().getYaw();
        float pitch = p.getLocation().getPitch();

        Bukkit.getScheduler().runTaskAsynchronously(plugin,
                () -> {
                    int assigneeID, advisorID;
                    try {
                        assigneeID = playerDAO.getPlayerID(ign);
                        advisorID = playerDAO.getPlayerID(uuid);
                    } catch (SQLException ex) {
                        Bukkit.getScheduler().runTask(plugin, () -> p.sendMessage(ChatColor.RED + ex.getMessage()));
                        ex.printStackTrace();
                        return;
                    } catch (DataAccessException ignored) {
                        Bukkit.getScheduler().runTask(plugin, () -> p.sendMessage(ChatColor.GOLD + "Player " + ign +
                                " is not registered as member. Use" + ChatColor.DARK_AQUA + " /addmember " + ign +
                                ChatColor.GOLD + " for adding player as member, then you can add tasks."));
                        return;
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
                                    if (Bukkit.getPluginManager().isPluginEnabled("DecentHolograms")) {
                                        HologramLayer.getInstance().setTasks(assigneeUUID, activeTasks);
                                    }
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
                                        DiscordManager.getInstance().taskCreated(discordUserID, p.getName(), task);
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
