package me.colormaestro.taskmanager.listeners;

import me.colormaestro.taskmanager.data.DataAccessException;
import me.colormaestro.taskmanager.data.PlayerDAO;
import me.colormaestro.taskmanager.data.TaskDAO;
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

public class ActiveTasksViewListener implements Listener {
    private final Plugin plugin;
    private final TaskDAO taskDAO;
    private final PlayerDAO playerDAO;
    private static final int INVENTORY_SIZE = 54;
    private static final int SHOW_BACK_POSITION = 49;

    public ActiveTasksViewListener(Plugin plugin, TaskDAO taskDAO, PlayerDAO playerDAO) {
        this.plugin = plugin;
        this.taskDAO = taskDAO;
        this.playerDAO = playerDAO;
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (event.getView().getTitle().contains(Directives.ACTIVE_TASKS)) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null) {
                return;
            }

            HumanEntity player = event.getView().getPlayer();
            switch (event.getCurrentItem().getType()) {
                case ORANGE_CONCRETE, LIME_CONCRETE -> handleConcreteClick(player, event.getCurrentItem());
                case LIGHT_BLUE_CONCRETE -> handleShowApprovedTasksClick(player, event.getCurrentItem());
                case SPECTRAL_ARROW -> handleSpectralArrowClick(player);
                case ARROW -> handleArrowClick();
            }
        }
    }

    private void handleConcreteClick(HumanEntity player, ItemStack headStack) {
        String taskId = headStack.getItemMeta().getDisplayName().split("#")[1];
        Bukkit.getScheduler().runTaskAsynchronously(plugin,
                SharedRunnables.teleportPlayerToTask(plugin, taskDAO, player, taskId));
    }

    private void handleShowApprovedTasksClick(HumanEntity player, ItemStack concreteStack) {
        String adjective = concreteStack.getItemMeta().getDisplayName().split(" ")[1];
        String ign = adjective.split("'")[0];
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
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
        });
    }

    private void handleSpectralArrowClick(HumanEntity player) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, SharedRunnables.showDashboardView(plugin, taskDAO, player));
    }

    private void handleArrowClick() {

    }
}
