package me.colormaestro.taskmanager.listeners;

import me.colormaestro.taskmanager.data.TaskDAO;
import me.colormaestro.taskmanager.model.MemberTaskStats;
import me.colormaestro.taskmanager.utils.Directives;
import me.colormaestro.taskmanager.utils.ItemStackBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.sql.SQLException;
import java.util.List;

public class ClickEventRunnables {
    private static final int INVENTORY_SIZE = 54;
    private static final int SHOW_SUPERVISED_TASKS_POSITION = 49;

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
}
