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
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.sql.SQLException;
import java.util.List;

public class InventoryClickListener implements Listener {
    private final Plugin plugin;
    private final TaskDAO taskDAO;
    private final PlayerDAO playerDAO;
    private static final int INVENTORY_SIZE = 54;
    private static final int SHOW_APPROVED_TASKS_POSITION = 49;

    public InventoryClickListener(Plugin plugin, TaskDAO taskDAO, PlayerDAO playerDAO) {
        this.plugin = plugin;
        this.taskDAO = taskDAO;
        this.playerDAO = playerDAO;
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (event.getView().getTitle().contains(Directives.DASHBOARD)) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null) {
                return;
            }

            HumanEntity player = event.getView().getPlayer();
            switch (event.getCurrentItem().getType()) {
                case PLAYER_HEAD -> handlePlayerHeadClick(player, event.getCurrentItem());
                case ARROW -> handleArrowClick();
            }
        }
    }

    private void handlePlayerHeadClick(HumanEntity player, ItemStack headStack) {
        String ign = headStack.getItemMeta().getDisplayName().replaceFirst(ChatColor.BLUE + "" + ChatColor.BOLD, "");
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                int id = playerDAO.getPlayerID(ign);
                List<Task> tasks = taskDAO.fetchPlayersActiveTasks(id);
                int totalPages = tasks.size() / (INVENTORY_SIZE - 9) + 1;
                Bukkit.getScheduler().runTask(plugin,
                        () -> {
                            String title = ChatColor.BLUE + "" + ChatColor.BOLD + ign + "'s tasks" + ChatColor.RESET + " (1/" + totalPages + ") " + Directives.DASHBOARD;
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

                            player.openInventory(inventory);
                        });
            } catch (SQLException | DataAccessException ex) {
                Bukkit.getScheduler().runTask(plugin,
                        () -> player.sendMessage(ChatColor.RED + ex.getMessage()));
                ex.printStackTrace();
            }
        });
    }

    private void handleArrowClick() {

    }
}
