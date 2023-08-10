package me.colormaestro.taskmanager.listeners;

import me.colormaestro.taskmanager.data.PlayerDAO;
import me.colormaestro.taskmanager.data.TaskDAO;
import me.colormaestro.taskmanager.utils.Directives;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class ApprovedTasksViewListener implements Listener {
    private final Plugin plugin;
    private final TaskDAO taskDAO;
    private final PlayerDAO playerDAO;

    public ApprovedTasksViewListener(Plugin plugin, TaskDAO taskDAO, PlayerDAO playerDAO) {
        this.plugin = plugin;
        this.taskDAO = taskDAO;
        this.playerDAO = playerDAO;
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (event.getView().getTitle().contains(Directives.APPROVED_TASKS)) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null) {
                return;
            }

            HumanEntity player = event.getView().getPlayer();
            switch (event.getCurrentItem().getType()) {
                case LIGHT_BLUE_CONCRETE -> handleConcreteClick(player, event.getCurrentItem());
                case SPECTRAL_ARROW -> handleSpectralArrowClick(player, event.getView());
                case ARROW -> handleArrowClick(player, event.getView(), event.getCurrentItem());
            }
        }
    }

    private void handleConcreteClick(HumanEntity player, ItemStack headStack) {
        String taskId = headStack.getItemMeta().getDisplayName().split("#")[1];
        Bukkit.getScheduler().runTaskAsynchronously(plugin,
                SharedRunnables.teleportPlayerToTask(plugin, taskDAO, player, taskId));
    }

    private void handleSpectralArrowClick(HumanEntity player, InventoryView view) {
        String ign = view.getTitle().replaceFirst(ChatColor.DARK_AQUA + "" + ChatColor.BOLD, "").split("'")[0];
        Bukkit.getScheduler().runTaskAsynchronously(plugin,
                SharedRunnables.showActiveTasksView(plugin, taskDAO, playerDAO, player, ign, 1));
    }

    private void handleArrowClick(HumanEntity player, InventoryView view, ItemStack arrow) {
        var parts = view.getTitle().replaceFirst(ChatColor.DARK_AQUA + "" + ChatColor.BOLD, "").split("['()/]");
        String ign = parts[0];
        long currentPage = Long.parseLong(parts[2]);
        long totalPages = Long.parseLong(parts[3]);

        if (arrow.getItemMeta().getDisplayName().contains("Next")) {
            currentPage++;
        } else {
            currentPage--;
        }

        if (currentPage > totalPages) {
            currentPage = 1;
        } else if (currentPage < 1) {
            currentPage = totalPages;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin,
                SharedRunnables.showApprovedTasksView(plugin, taskDAO, playerDAO, player, ign, currentPage));
    }
}
