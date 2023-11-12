package me.colormaestro.taskmanager.listeners;

import me.colormaestro.taskmanager.data.DataAccessException;
import me.colormaestro.taskmanager.integrations.DiscordOperator;
import me.colormaestro.taskmanager.data.MemberDAO;
import me.colormaestro.taskmanager.data.TaskDAO;
import me.colormaestro.taskmanager.enums.TaskStatus;
import me.colormaestro.taskmanager.integrations.DecentHologramsIntegration;
import me.colormaestro.taskmanager.integrations.DynmapIntegration;
import me.colormaestro.taskmanager.model.Member;
import me.colormaestro.taskmanager.model.Task;
import me.colormaestro.taskmanager.utils.DataContainerKeys;
import me.colormaestro.taskmanager.utils.Directives;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class BookEditListener implements Listener {
    private final Plugin plugin;
    private final TaskDAO taskDAO;
    private final MemberDAO memberDAO;
    private final DecentHologramsIntegration decentHolograms;
    private final DynmapIntegration dynmap;

    public BookEditListener(Plugin plugin, TaskDAO taskDAO, MemberDAO memberDAO,
                            DecentHologramsIntegration decentHolograms, DynmapIntegration dynmap) {
        this.plugin = plugin;
        this.taskDAO = taskDAO;
        this.memberDAO = memberDAO;
        this.decentHolograms = decentHolograms;
        this.dynmap = dynmap;
    }

    @EventHandler
    public void onBookEdit(PlayerEditBookEvent event) {
        var dataContainer = event.getPreviousBookMeta().getPersistentDataContainer();
        String directive = dataContainer.get(new NamespacedKey(plugin, DataContainerKeys.BOOK_ACTION), PersistentDataType.STRING);
        if (!event.getNewBookMeta().hasAuthor() || directive == null) {
            return;
        }

        Player p = event.getPlayer();
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

        if (directive.equals(Directives.CREATE_TASK)) {
            String memberName = dataContainer.get(
                    new NamespacedKey(plugin, DataContainerKeys.MEMBER_NAME), PersistentDataType.STRING);
            Bukkit.getScheduler().runTaskAsynchronously(plugin,
                    createDoingTask(p, memberName, p.getUniqueId(), title, description, x, y, z, yaw, pitch));
        }

        if (directive.equals(Directives.PREPARE_TASK)) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin,
                    createPreparedTask(p, p.getUniqueId(), title, description, x, y, z, yaw, pitch));
        }
    }

    /**
     * Creates job for creation of task in {@link me.colormaestro.taskmanager.enums.TaskStatus#DOING} state
     *
     * @return Runnable (job) for execution
     */
    private Runnable createDoingTask(
            Player p,
            String ign,
            UUID uuid,
            String title,
            String description,
            double x,
            double y,
            double z,
            float yaw,
            float pitch) {
        return () -> {
            Member assignee, advisor;
            try {
                assignee = memberDAO.findMember(ign);
                advisor = memberDAO.findMember(uuid);
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

            Date currentDate = new Date(System.currentTimeMillis());
            Task task = new Task(title, description, advisor.getId(), assignee.getId(), advisor.getId(), x, y, z, yaw,
                    pitch, TaskStatus.DOING, currentDate, currentDate, null);
            try {
                int taskID = taskDAO.createTask(task);
                List<Task> activeTasks = taskDAO.fetchPlayersActiveTasks(assignee.getId());
                Bukkit.getScheduler().runTask(plugin, () -> {
                    p.sendMessage(ChatColor.GREEN + "Task added.");

                    // Firstly we try to notify the assignee in game
                    boolean messageSent = false;
                    decentHolograms.setTasks(assignee.getUuid(), activeTasks);
                    String markerLabel = "[" + taskID + "] " + title;
                    dynmap.addTaskInProgressMarker(String.valueOf(taskID), markerLabel, p.getLocation());
                    for (Player target : Bukkit.getOnlinePlayers()) {
                        if (target.getUniqueId().toString().equals(assignee.getUuid())) {
                            target.sendMessage(ChatColor.GOLD + "You have new task from " + p.getName());
                            target.playSound(target.getLocation(),
                                    "minecraft:record.newtask", 10, 1);
                            messageSent = true;
                            break;
                        }
                    }

                    // If the assignee is not online, sent him message to discord
                    if (!messageSent && assignee.getDiscordID() != null) {
                        DiscordOperator.getInstance().taskCreated(assignee.getDiscordID(), p.getName(), task);
                    }
                });
            } catch (SQLException | IllegalArgumentException ex) {
                Bukkit.getScheduler().runTask(plugin,
                        () -> p.sendMessage(ChatColor.RED + ex.getMessage()));
                ex.printStackTrace();
            }
        };
    }

    /**
     * Creates job for creation of task in {@link me.colormaestro.taskmanager.enums.TaskStatus#PREPARED} state
     *
     * @return Runnable (job) for execution
     */
    private Runnable createPreparedTask(
            Player p,
            UUID uuid,
            String title,
            String description,
            double x,
            double y,
            double z,
            float yaw,
            float pitch) {
        return () -> {
            Member creator;
            try {
                creator = memberDAO.findMember(uuid);
            } catch (SQLException | DataAccessException ex) {
                Bukkit.getScheduler().runTask(plugin, () -> p.sendMessage(ChatColor.RED + ex.getMessage()));
                ex.printStackTrace();
                return;
            }

            Task task = new Task(title, description, creator.getId(), null, null, x, y, z, yaw, pitch,
                    TaskStatus.PREPARED, new Date(System.currentTimeMillis()), null, null);
            try {
                taskDAO.createTask(task);
                Bukkit.getScheduler().runTask(plugin, () -> p.sendMessage(ChatColor.GREEN + "Task prepared."));
            } catch (SQLException | IllegalArgumentException ex) {
                Bukkit.getScheduler().runTask(plugin,
                        () -> p.sendMessage(ChatColor.RED + ex.getMessage()));
                ex.printStackTrace();
            }
        };
    }
}
