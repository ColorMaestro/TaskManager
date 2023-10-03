package me.colormaestro.taskmanager.listeners;

import me.colormaestro.taskmanager.data.DataAccessException;
import me.colormaestro.taskmanager.data.HologramLayer;
import me.colormaestro.taskmanager.data.MemberDAO;
import me.colormaestro.taskmanager.data.TaskDAO;
import me.colormaestro.taskmanager.model.Member;
import me.colormaestro.taskmanager.model.Task;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class PlayerJoinListener implements Listener {
    private final Plugin plugin;
    private final TaskDAO taskDAO;
    private final MemberDAO memberDAO;

    public PlayerJoinListener(Plugin plugin, TaskDAO taskDAO, MemberDAO memberDAO) {
        this.plugin = plugin;
        this.taskDAO = taskDAO;
        this.memberDAO = memberDAO;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (Bukkit.getPluginManager().isPluginEnabled("DecentHolograms")) {
            Bukkit.getScheduler().runTaskLater(plugin, checkHologram(event), 180);
        }
        Bukkit.getScheduler().runTaskLater(plugin, checkDiscordID(event, plugin, memberDAO), 190);
        Bukkit.getScheduler().runTaskLater(plugin, checkFinishedTasks(event, plugin, taskDAO, memberDAO), 200);
    }

    private static Runnable checkHologram(PlayerJoinEvent event) {
        return () -> {
            String uuid = event.getPlayer().getUniqueId().toString();
            if (!HologramLayer.getInstance().hologramExists(uuid)) {
                event.getPlayer().sendMessage(ChatColor.DARK_AQUA +
                        "⚠ Your visual task list has not been established yet");
                event.getPlayer().sendMessage(ChatColor.DARK_AQUA +
                        "⚠ To do so issue command" + ChatColor.GOLD + "" + ChatColor.BOLD +
                        " /establish" + ChatColor.DARK_AQUA + " on the place, where you want to have it");
            }
        };
    }

    /**
     * Represents job, which checks, whether player has set discord_id in database and send notification if discord_id
     * is not set.
     *
     * @param event     PlayerJoinEvent
     * @param plugin    under which to run the job
     * @param memberDAO object for communication with database
     * @return Runnable (job) for execution
     */
    private static Runnable checkDiscordID(PlayerJoinEvent event, Plugin plugin, MemberDAO memberDAO) {
        return () -> {
            UUID uuid = event.getPlayer().getUniqueId();
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    Member member = memberDAO.findMember(uuid.toString());
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (member.getDiscordID() == null) {
                            event.getPlayer().sendMessage(ChatColor.BLUE
                                    + "ℹ You don't have linked your discord account yet. If you want to receive");
                            event.getPlayer().sendMessage(ChatColor.BLUE +
                                    "ℹ  notifications about new or approved tasks issue command "
                                    + ChatColor.GOLD + "" + ChatColor.BOLD + "/linkdiscord");

                        }
                    });
                } catch (SQLException ex) {
                    ex.printStackTrace();
                } catch (DataAccessException ignored) {
                    // Since this job has purely informative character we can ignore missing record in database.
                }
            });
        };
    }

    /**
     * Represents job, which checks, whether there are some finished tasks, in which the player figures as advisor
     * and sends them to the advisor
     *
     * @param event     PlayerJoinEvent
     * @param plugin    under which to run the job
     * @param memberDAO object for communication with database
     * @return Runnable (job) for execution
     */
    private static Runnable checkFinishedTasks(PlayerJoinEvent event, Plugin plugin, TaskDAO taskDAO, MemberDAO memberDAO) {
        return () -> {
            UUID uuid = event.getPlayer().getUniqueId();
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    Member advisor = memberDAO.findMember(uuid);
                    List<Task> finishedTasks = taskDAO.fetchFinishedTasks(advisor.getId());
                    Bukkit.getScheduler().runTask(plugin, () -> sendFinishedTasks(event.getPlayer(), finishedTasks));
                } catch (SQLException ex) {
                    ex.printStackTrace();
                } catch (DataAccessException ignored) {
                    // Since this job has purely informative character we can ignore missing record in database.
                }
            });
        };
    }

    private static void sendFinishedTasks(Player p, List<Task> tasks) {
        if (tasks.isEmpty()) {
            return;
        }
        p.sendMessage(ChatColor.GREEN + "-=-=-=- New finished tasks -=-=-=-");
        for (Task task : tasks) {
            p.sendMessage(ChatColor.GREEN + "[" + task.getId() + "] " + ChatColor.WHITE + task.getTitle());
        }
    }
}
