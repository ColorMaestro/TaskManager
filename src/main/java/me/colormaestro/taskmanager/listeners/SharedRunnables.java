package me.colormaestro.taskmanager.listeners;

import me.colormaestro.taskmanager.data.DataAccessException;
import me.colormaestro.taskmanager.data.PlayerDAO;
import me.colormaestro.taskmanager.data.TaskDAO;
import me.colormaestro.taskmanager.model.AdvisedTask;
import me.colormaestro.taskmanager.model.MemberTaskStats;
import me.colormaestro.taskmanager.model.Task;
import me.colormaestro.taskmanager.utils.Directives;
import me.colormaestro.taskmanager.utils.InventoryBuilder;
import me.colormaestro.taskmanager.utils.ItemStackCreator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.sql.SQLException;
import java.util.List;

public class SharedRunnables {
    private static final int PAGE_SIZE = 45;
    private static final int LAST_ROW_MIDDLE = 49;
    private static final int LAST_ROW_LEFT_FROM_MIDDLE = 48;

    public static Runnable showDashboardView(Plugin plugin, TaskDAO taskDAO, HumanEntity player, long page) {
        return () -> {
            try {
                List<MemberTaskStats> stats = taskDAO.fetchTaskStatistics();
                int totalPages = stats.size() / PAGE_SIZE + 1;
                // Variable used in lambda should be final or effectively final
                List<MemberTaskStats> finalStats = stats.stream().skip((page - 1) * PAGE_SIZE).limit(PAGE_SIZE).toList();
                Bukkit.getScheduler().runTask(plugin, () -> {
                    String inventoryTitle = ChatColor.BLUE + "" + ChatColor.BOLD + "Tasks Dashboard" + ChatColor.RESET + " (" + page + "/" + totalPages + ") " + Directives.DASHBOARD;
                    InventoryBuilder builder = new InventoryBuilder(player, inventoryTitle);

                    ItemStack stack;
                    int position = 0;
                    for (MemberTaskStats memberStats : finalStats) {
                        stack = ItemStackCreator.createMemberStack(memberStats.uuid(), memberStats.ign(), memberStats.doing(), memberStats.finished(), memberStats.approved());
                        builder.addItemStack(position, stack);
                        position++;
                    }

                    builder.addPaginationArrows()
                            .addItemStack(LAST_ROW_MIDDLE, Material.ENDER_EYE,
                                    ChatColor.DARK_PURPLE + "Show supervised tasks");

                    player.openInventory(builder.build());
                });
            } catch (SQLException ex) {
                Bukkit.getScheduler().runTask(plugin,
                        () -> player.sendMessage(ChatColor.RED + ex.getMessage()));
                ex.printStackTrace();
            }
        };
    }

    public static Runnable showActiveTasksView(Plugin plugin, TaskDAO taskDAO, PlayerDAO playerDAO, HumanEntity player, String ign, long page) {
        return () -> {
            try {
                int id = playerDAO.getPlayerID(ign);
                List<Task> tasks = taskDAO.fetchPlayersActiveTasks(id);
                int totalPages = tasks.size() / PAGE_SIZE + 1;
                List<Task> finalTasks = tasks.stream().skip((page - 1) * PAGE_SIZE).limit(PAGE_SIZE).toList();
                Bukkit.getScheduler().runTask(plugin, () -> {
                    String title = ChatColor.BLUE + "" + ChatColor.BOLD + ign + "'s tasks" + ChatColor.RESET + " (" + page + "/" + totalPages + ") " + Directives.ACTIVE_TASKS;
                    InventoryBuilder builder = new InventoryBuilder(player, title);

                    ItemStack stack;
                    int position = 0;
                    for (Task task : finalTasks) {
                        stack = ItemStackCreator.createTaskStack(
                                task.getId(),
                                task.getTitle(),
                                task.getDescription(),
                                task.getStatus(),
                                null);
                        builder.addItemStack(position, stack);
                        position++;
                    }

                    builder.addPaginationArrows()
                            .addItemStack(LAST_ROW_MIDDLE, Material.LIGHT_BLUE_CONCRETE,
                                    ChatColor.AQUA + "Show " + ign + "'s approved tasks")
                            .addItemStack(LAST_ROW_LEFT_FROM_MIDDLE, Material.SPECTRAL_ARROW,
                                    ChatColor.AQUA + "Back to dashboard");

                    player.openInventory(builder.build());
                });
            } catch (SQLException | DataAccessException ex) {
                Bukkit.getScheduler().runTask(plugin,
                        () -> player.sendMessage(ChatColor.RED + ex.getMessage()));
                ex.printStackTrace();
            }
        };
    }

    public static Runnable showApprovedTasksView(Plugin plugin, TaskDAO taskDAO, PlayerDAO playerDAO, HumanEntity player, String ign, long page) {
        return () -> {
            try {
                int id = playerDAO.getPlayerID(ign);
                List<Task> tasks = taskDAO.fetchPlayersApprovedTasks(id);
                int totalPages = tasks.size() / PAGE_SIZE + 1;
                List<Task> finalTasks = tasks.stream().skip((page - 1) * PAGE_SIZE).limit(PAGE_SIZE).toList();
                Bukkit.getScheduler().runTask(plugin, () -> {
                    String title = ChatColor.DARK_AQUA + "" + ChatColor.BOLD + ign + "'s approved tasks" + ChatColor.RESET + " (" + page + "/" + totalPages + ") " + Directives.APPROVED_TASKS;
                    InventoryBuilder builder = new InventoryBuilder(player, title);

                    ItemStack stack;
                    int position = 0;
                    for (Task task : finalTasks) {
                        stack = ItemStackCreator.createTaskStack(
                                task.getId(),
                                task.getTitle(),
                                task.getDescription(),
                                task.getStatus(),
                                null);
                        builder.addItemStack(position, stack);
                        position++;
                    }

                    builder.addPaginationArrows()
                            .addItemStack(LAST_ROW_MIDDLE, Material.SPECTRAL_ARROW,
                                    ChatColor.AQUA + "Back to active tasks");

                    player.openInventory(builder.build());
                });
            } catch (SQLException | DataAccessException ex) {
                Bukkit.getScheduler().runTask(plugin,
                        () -> player.sendMessage(ChatColor.RED + ex.getMessage()));
                ex.printStackTrace();
            }
        };
    }

    public static Runnable showSupervisedTasksView(Plugin plugin, TaskDAO taskDAO, PlayerDAO playerDAO, HumanEntity player, long page) {
        return () -> {
            try {
                int id = playerDAO.getPlayerID(player.getName());
                List<AdvisedTask> tasks = taskDAO.fetchAdvisorActiveTasks(id);
                int totalPages = tasks.size() / PAGE_SIZE + 1;
                List<AdvisedTask> finalTasks = tasks.stream().skip((page - 1) * PAGE_SIZE).limit(PAGE_SIZE).toList();
                Bukkit.getScheduler().runTask(plugin, () -> {
                    String title = ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Your supervised tasks" + ChatColor.RESET + " (" + page + "/" + totalPages + ") " + Directives.SUPERVISED_TASKS;
                    InventoryBuilder builder = new InventoryBuilder(player, title);

                    ItemStack stack;
                    int position = 0;
                    for (AdvisedTask task : finalTasks) {
                        stack = ItemStackCreator.createTaskStack(
                                task.id(),
                                task.title(),
                                task.description(),
                                task.status(),
                                task.ign());
                        builder.addItemStack(position, stack);
                        position++;
                    }

                    builder.addPaginationArrows()
                            .addItemStack(LAST_ROW_MIDDLE, Material.SPECTRAL_ARROW,
                                    ChatColor.AQUA + "Back to dashboard");

                    player.openInventory(builder.build());
                });
            } catch (SQLException | DataAccessException ex) {
                Bukkit.getScheduler().runTask(plugin,
                        () -> player.sendMessage(ChatColor.RED + ex.getMessage()));
                ex.printStackTrace();
            }
        };
    }

    public static Runnable teleportPlayerToTask(Plugin plugin, TaskDAO taskDAO, HumanEntity player, String taskId) {
        return () -> {
            try {
                Task task = taskDAO.findTask(Integer.parseInt(taskId));
                Bukkit.getScheduler().runTask(plugin,
                        () -> {
                            double x = task.getX();
                            double y = task.getY();
                            double z = task.getZ();
                            float yaw = task.getYaw();
                            float pitch = task.getPitch();

                            player.closeInventory();
                            player.teleport(new Location(player.getWorld(), x, y, z, yaw, pitch));
                        });
            } catch (SQLException | DataAccessException ex) {
                Bukkit.getScheduler().runTask(plugin,
                        () -> player.sendMessage(ChatColor.RED + ex.getMessage()));
                ex.printStackTrace();
            }
        };
    }
}
