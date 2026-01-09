package me.colormaestro.taskmanager.listeners;

import me.colormaestro.taskmanager.data.DataAccessException;
import me.colormaestro.taskmanager.data.MemberDAO;
import me.colormaestro.taskmanager.data.TaskDAO;
import me.colormaestro.taskmanager.integrations.DecentHologramsIntegration;
import me.colormaestro.taskmanager.model.Member;
import me.colormaestro.taskmanager.model.Task;
import me.colormaestro.taskmanager.tabcompleters.ReloadableTabCompleter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.sql.SQLException;
import java.util.List;

public class PlayerJoinListener implements Listener {
    private static final BukkitScheduler scheduler = Bukkit.getScheduler();
    private final Plugin plugin;
    private final TaskDAO taskDAO;
    private final MemberDAO memberDAO;
    private final ReloadableTabCompleter completer;
    private final ReloadableTabCompleter completerA;
    private final DecentHologramsIntegration decentHolograms;

    public PlayerJoinListener(Plugin plugin, TaskDAO taskDAO, MemberDAO memberDAO,
                              ReloadableTabCompleter completer, ReloadableTabCompleter completerA,
                              DecentHologramsIntegration decentHolograms) {
        this.plugin = plugin;
        this.taskDAO = taskDAO;
        this.memberDAO = memberDAO;
        this.completer = completer;
        this.completerA = completerA;
        this.decentHolograms = decentHolograms;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        scheduler.runTask(plugin, updateMemberLoginTime(event, plugin, memberDAO));
        scheduler.runTaskLater(plugin, checkMemberNameUpdate(event, plugin, memberDAO, completer, completerA), 20);
        scheduler.runTaskLater(plugin, checkHologram(event, plugin, memberDAO), 180);
        scheduler.runTaskLater(plugin, checkDiscordID(event, plugin, memberDAO), 190);
        scheduler.runTaskLater(plugin, checkFinishedTasks(event, plugin, taskDAO, memberDAO), 200);
    }

    private static Runnable updateMemberLoginTime(PlayerJoinEvent event, Plugin plugin, MemberDAO memberDAO) {
        return () -> {
            Player player = event.getPlayer();
            scheduler.runTaskAsynchronously(plugin, () -> {
                try {
                    memberDAO.updateLastLoginTime(player.getUniqueId());
                } catch (SQLException ex) {
                    player.sendMessage(ChatColor.RED + ex.getMessage());
                    ex.printStackTrace();
                }
            });
        };
    }

    /**
     * @param event      PlayerJoinEvent
     * @param plugin     under which to run the job
     * @param memberDAO  object for communication with database
     * @param completer
     * @param completerA
     * @return Runnable (job) for execution
     */
    private static Runnable checkMemberNameUpdate(PlayerJoinEvent event, Plugin plugin, MemberDAO memberDAO,
                                                  ReloadableTabCompleter completer, ReloadableTabCompleter completerA) {
        return () -> {
            Player player = event.getPlayer();
            scheduler.runTaskAsynchronously(plugin, () -> {
                try {
                    Member member = memberDAO.findMember(player.getUniqueId());
                    if (!player.getName().equals(member.getIgn())) {
                        memberDAO.updateMemberName(player.getUniqueId(), player.getName());
                        completer.reload();
                        completerA.reload();
                        scheduler.runTask(plugin, () -> event.getPlayer().sendMessage(ChatColor.DARK_AQUA +
                                "It seems that you changed your name, hereby it was updated in database."));
                    }
                } catch (SQLException ex) {
                    player.sendMessage(ChatColor.RED + ex.getMessage());
                    ex.printStackTrace();
                } catch (DataAccessException ignored) {
                    // Ignored if player is not a member
                }
            });
        };
    }

    private Runnable checkHologram(PlayerJoinEvent event, Plugin plugin, MemberDAO memberDAO) {
        return () -> {
            Player player = event.getPlayer();
            scheduler.runTaskAsynchronously(plugin, () -> {
                try {
                    if (memberDAO.memberExists(player.getUniqueId().toString())) {
                        scheduler.runTask(plugin, () -> {
                            if (!decentHolograms.hologramExists(player.getUniqueId().toString())) {
                                event.getPlayer().sendMessage(ChatColor.DARK_AQUA +
                                        "⚠ Your visual task list has not been established yet");
                                event.getPlayer().sendMessage(ChatColor.DARK_AQUA +
                                        "⚠ To do so issue command" + ChatColor.GOLD + ChatColor.BOLD +
                                        " /establish" + ChatColor.DARK_AQUA + " on the place, where you want to have it");
                            }
                        });
                    }
                } catch (SQLException e) {
                    player.sendMessage(ChatColor.RED + e.getMessage());
                }
            });
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
            Player player = event.getPlayer();
            scheduler.runTaskAsynchronously(plugin, () -> {
                try {
                    Member member = memberDAO.findMember(player.getUniqueId());
                    scheduler.runTask(plugin, () -> {
                        if (member.getDiscordID() == null) {
                            event.getPlayer().sendMessage(ChatColor.BLUE
                                    + "ℹ You don't have linked your discord account yet. If you want to receive");
                            event.getPlayer().sendMessage(ChatColor.BLUE +
                                    "ℹ  notifications about new or approved tasks issue command "
                                    + ChatColor.GOLD + ChatColor.BOLD + "/linkdiscord");

                        }
                    });
                } catch (SQLException ex) {
                    player.sendMessage(ChatColor.RED + ex.getMessage());
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
            Player player = event.getPlayer();
            scheduler.runTaskAsynchronously(plugin, () -> {
                try {
                    Member advisor = memberDAO.findMember(player.getUniqueId());
                    List<Task> finishedTasks = taskDAO.fetchFinishedTasks(advisor.getId());
                    scheduler.runTask(plugin, () -> sendFinishedTasks(event.getPlayer(), finishedTasks));
                } catch (SQLException ex) {
                    player.sendMessage(ChatColor.RED + ex.getMessage());
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
