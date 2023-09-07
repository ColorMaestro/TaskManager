package me.colormaestro.taskmanager.listeners;

import me.colormaestro.taskmanager.data.DataAccessException;
import me.colormaestro.taskmanager.data.PlayerDAO;
import me.colormaestro.taskmanager.data.TaskDAO;
import me.colormaestro.taskmanager.model.MemberTaskStats;
import me.colormaestro.taskmanager.model.Task;
import me.colormaestro.taskmanager.utils.Directives;
import me.colormaestro.taskmanager.utils.ItemStackCreator;
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
                Bukkit.getScheduler().runTask(plugin,
                        () -> {
                            String inventoryTitle = ChatColor.BLUE + "" + ChatColor.BOLD + "Tasks Dashboard" + ChatColor.RESET + " (" + page + "/" + totalPages + ") " + Directives.DASHBOARD;
                            Inventory inventory = Bukkit.createInventory(player, INVENTORY_SIZE, inventoryTitle);

                            ItemStack stack;
                            int position = 0;
                            for (MemberTaskStats memberStats : finalStats) {
                                stack = ItemStackCreator.createMemberStack(memberStats.uuid(), memberStats.ign(), memberStats.doing(), memberStats.finished(), memberStats.approved());
                                inventory.setItem(position, stack);
                                position++;
                            }

                            ItemStackCreator.supplyInventoryWithPaginationArrows(inventory);

                            stack = new ItemStack(Material.ENDER_EYE, 1);
                            ItemMeta meta = stack.getItemMeta();
                            assert meta != null;
                            meta.setDisplayName(ChatColor.DARK_PURPLE + "Show supervised tasks");
                            stack.setItemMeta(meta);
                            inventory.setItem(LAST_ROW_MIDDLE, stack);

                            player.openInventory(inventory);
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
                Bukkit.getScheduler().runTask(plugin,
                        () -> {
                            String title = ChatColor.BLUE + "" + ChatColor.BOLD + ign + "'s tasks" + ChatColor.RESET + " (" + page + "/" + totalPages + ") " + Directives.ACTIVE_TASKS;
                            Inventory inventory = Bukkit.createInventory(player, INVENTORY_SIZE, title);

                            ItemStack stack;
                            int position = 0;
                            for (Task task : finalTasks) {
                                stack = ItemStackCreator.createTaskStack(task);
                                inventory.setItem(position, stack);
                                position++;
                            }

                            ItemStackCreator.supplyInventoryWithPaginationArrows(inventory);

                            stack = new ItemStack(Material.LIGHT_BLUE_CONCRETE, 1);
                            ItemMeta meta = stack.getItemMeta();
                            assert meta != null;
                            meta.setDisplayName(ChatColor.AQUA + "Show " + ign + "'s approved tasks");
                            stack.setItemMeta(meta);
                            inventory.setItem(LAST_ROW_MIDDLE, stack);

                            stack = new ItemStack(Material.SPECTRAL_ARROW, 1);
                            meta = stack.getItemMeta();
                            assert meta != null;
                            meta.setDisplayName(ChatColor.AQUA + "Back to dashboard");
                            stack.setItemMeta(meta);
                            inventory.setItem(LAST_ROW_LEFT_FROM_MIDDLE, stack);

                            player.openInventory(inventory);
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
                Bukkit.getScheduler().runTask(plugin,
                        () -> {
                            String title = ChatColor.DARK_AQUA + "" + ChatColor.BOLD + ign + "'s approved tasks" + ChatColor.RESET + " (" + page + "/" + totalPages + ") " + Directives.APPROVED_TASKS;
                            Inventory inventory = Bukkit.createInventory(player, INVENTORY_SIZE, title);

                            ItemStack stack;
                            int position = 0;
                            for (Task task : finalTasks) {
                                stack = ItemStackCreator.createTaskStack(task);
                                inventory.setItem(position, stack);
                                position++;
                            }

                            ItemStackCreator.supplyInventoryWithPaginationArrows(inventory);

                            stack = new ItemStack(Material.SPECTRAL_ARROW, 1);
                            ItemMeta meta = stack.getItemMeta();
                            assert meta != null;
                            meta.setDisplayName(ChatColor.AQUA + "Back to active tasks");
                            stack.setItemMeta(meta);
                            inventory.setItem(LAST_ROW_MIDDLE, stack);

                            player.openInventory(inventory);
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
                List<Task> tasks = taskDAO.fetchAdvisorActiveTasks(id);
                int totalPages = tasks.size() / PAGE_SIZE + 1;
                List<Task> finalTasks = tasks.stream().skip((page - 1) * PAGE_SIZE).limit(PAGE_SIZE).toList();
                Bukkit.getScheduler().runTask(plugin,
                        () -> {
                            String title = ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Your supervised tasks" + ChatColor.RESET + " (" + page + "/" + totalPages + ") " + Directives.SUPERVISED_TASKS;
                            Inventory inventory = Bukkit.createInventory(player, INVENTORY_SIZE, title);

                            ItemStack stack;
                            int position = 0;
                            for (Task task : finalTasks) {
                                stack = ItemStackCreator.createTaskStack(task);
                                inventory.setItem(position, stack);
                                position++;
                            }

                            ItemStackCreator.supplyInventoryWithPaginationArrows(inventory);

                            stack = new ItemStack(Material.SPECTRAL_ARROW, 1);
                            ItemMeta meta = stack.getItemMeta();
                            assert meta != null;
                            meta.setDisplayName(ChatColor.AQUA + "Back to dashboard");
                            stack.setItemMeta(meta);
                            inventory.setItem(LAST_ROW_MIDDLE, stack);

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
