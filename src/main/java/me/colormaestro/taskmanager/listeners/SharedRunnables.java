package me.colormaestro.taskmanager.listeners;

import me.colormaestro.taskmanager.data.DataAccessException;
import me.colormaestro.taskmanager.data.PlayerDAO;
import me.colormaestro.taskmanager.data.TaskDAO;
import me.colormaestro.taskmanager.model.MemberTaskStats;
import me.colormaestro.taskmanager.model.Task;
import me.colormaestro.taskmanager.utils.Directives;
import me.colormaestro.taskmanager.utils.ItemStackBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.sql.SQLException;
import java.util.List;

public class SharedRunnables {
    private static final int INVENTORY_SIZE = 54;
    private static final int SHOW_SUPERVISED_TASKS_POSITION = 49;
    private static final int SHOW_APPROVED_TASKS_POSITION = 49;
    private static final int SHOW_DASHBOARD_POSITION = 48;
    private static final int SHOW_BACK_POSITION = 49;

    public static Runnable showDashboardView(Plugin plugin, TaskDAO taskDAO, HumanEntity player) {
        return () -> {
            try {
                List<MemberTaskStats> stats = taskDAO.fetchTaskStatistics();
                int totalPages = stats.size() / (INVENTORY_SIZE - 9) + 1;
                // Variable used in lambda should be final or effectively final
                List<MemberTaskStats> finalStats = stats.stream().limit(INVENTORY_SIZE - 9).toList();
                Bukkit.getScheduler().runTask(plugin,
                        () -> {
                            String inventoryTitle = ChatColor.BLUE + "" + ChatColor.BOLD + "Tasks Dashboard" + ChatColor.RESET + " (1/" + totalPages + ") " + Directives.DASHBOARD;
                            Inventory inventory = Bukkit.createInventory(player, INVENTORY_SIZE, inventoryTitle);

                            ItemStack stack;
                            int position = 0;
                            for (MemberTaskStats memberStats : finalStats) {
                                stack = ItemStackBuilder.buildMemberStack(memberStats.uuid(), memberStats.ign(), memberStats.doing(), memberStats.finished(), memberStats.approved());
                                inventory.setItem(position, stack);
                                position++;
                            }

                            ItemStackBuilder.supplyInventoryWithPaginationArrows(inventory);

                            stack = new ItemStack(Material.ENDER_EYE, 1);
                            ItemMeta meta = stack.getItemMeta();
                            assert meta != null;
                            meta.setDisplayName(ChatColor.DARK_PURPLE + "Show supervised tasks");
                            stack.setItemMeta(meta);
                            inventory.setItem(SHOW_SUPERVISED_TASKS_POSITION, stack);

                            player.openInventory(inventory);
                        });
            } catch (SQLException ex) {
                Bukkit.getScheduler().runTask(plugin,
                        () -> player.sendMessage(ChatColor.RED + ex.getMessage()));
                ex.printStackTrace();
            }
        };
    }

    public static Runnable showActiveTasksView(Plugin plugin, TaskDAO taskDAO, PlayerDAO playerDAO, HumanEntity player, String ign) {
        return () -> {
            try {
                int id = playerDAO.getPlayerID(ign);
                List<Task> tasks = taskDAO.fetchPlayersActiveTasks(id);
                int totalPages = tasks.size() / (INVENTORY_SIZE - 9) + 1;
                Bukkit.getScheduler().runTask(plugin,
                        () -> {
                            String title = ChatColor.BLUE + "" + ChatColor.BOLD + ign + "'s tasks" + ChatColor.RESET + " (1/" + totalPages + ") " + Directives.ACTIVE_TASKS;
                            Inventory inventory = Bukkit.createInventory(player, INVENTORY_SIZE, title);

                            ItemStack stack;
                            int position = 0;
                            for (Task task : tasks) {
                                stack = ItemStackBuilder.buildTaskStack(task);
                                inventory.setItem(position, stack);
                                position++;
                            }

                            ItemStackBuilder.supplyInventoryWithPaginationArrows(inventory);

                            stack = new ItemStack(Material.LIGHT_BLUE_CONCRETE, 1);
                            ItemMeta meta = stack.getItemMeta();
                            assert meta != null;
                            meta.setDisplayName(ChatColor.AQUA + "Show " + ign + "'s approved tasks");
                            stack.setItemMeta(meta);
                            inventory.setItem(SHOW_APPROVED_TASKS_POSITION, stack);

                            stack = new ItemStack(Material.SPECTRAL_ARROW, 1);
                            meta = stack.getItemMeta();
                            assert meta != null;
                            meta.setDisplayName(ChatColor.AQUA + "Back to dashboard");
                            stack.setItemMeta(meta);
                            inventory.setItem(SHOW_DASHBOARD_POSITION, stack);

                            player.openInventory(inventory);
                        });
            } catch (SQLException | DataAccessException ex) {
                Bukkit.getScheduler().runTask(plugin,
                        () -> player.sendMessage(ChatColor.RED + ex.getMessage()));
                ex.printStackTrace();
            }
        };
    }

    public static Runnable showApprovedTasksView(Plugin plugin, TaskDAO taskDAO, PlayerDAO playerDAO, HumanEntity player, String ign) {
        return () -> {
            try {
                int id = playerDAO.getPlayerID(ign);
                List<Task> tasks = taskDAO.fetchPlayersApprovedTasks(id);
                int totalPages = tasks.size() / (INVENTORY_SIZE - 9) + 1;
                Bukkit.getScheduler().runTask(plugin,
                        () -> {
                            String title = ChatColor.DARK_AQUA + "" + ChatColor.BOLD + ign + "'s approved tasks" + ChatColor.RESET + " (1/" + totalPages + ") " + Directives.APPROVED_TASKS;
                            Inventory inventory = Bukkit.createInventory(player, INVENTORY_SIZE, title);

                            ItemStack stack;
                            int position = 0;
                            for (Task task : tasks) {
                                stack = ItemStackBuilder.buildTaskStack(task);
                                inventory.setItem(position, stack);
                                position++;
                            }

                            ItemStackBuilder.supplyInventoryWithPaginationArrows(inventory);

                            stack = new ItemStack(Material.SPECTRAL_ARROW, 1);
                            ItemMeta meta = stack.getItemMeta();
                            assert meta != null;
                            meta.setDisplayName(ChatColor.AQUA + "Back to active tasks");
                            stack.setItemMeta(meta);
                            inventory.setItem(SHOW_BACK_POSITION, stack);

                            player.openInventory(inventory);
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
