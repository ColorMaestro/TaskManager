package me.colormaestro.taskmanager.listeners;

import me.colormaestro.taskmanager.utils.Directives;
import me.colormaestro.taskmanager.utils.RunnablesCreator;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class PreparedTasksViewListener implements Listener {
    private final RunnablesCreator creator;

    public PreparedTasksViewListener(RunnablesCreator creator) {
        this.creator = creator;
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (event.getView().getTitle().contains(Directives.PREPARED_TASKS)) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null) {
                return;
            }

            HumanEntity player = event.getView().getPlayer();
            switch (event.getCurrentItem().getType()) {
                case LIGHT_GRAY_CONCRETE -> handleConcreteClick(player, event.getCurrentItem());
                case SPECTRAL_ARROW -> handleSpectralArrowClick(player);
                case ARROW -> handleArrowClick(player, event.getView(), event.getCurrentItem());
            }
        }
    }

    private void handleConcreteClick(HumanEntity player, ItemStack headStack) {
        String taskId = headStack.getItemMeta().getDisplayName().split("#")[1];
        Bukkit.getScheduler().runTaskAsynchronously(creator.getPlugin(), creator.teleportPlayerToTask(player, taskId));
    }

    private void handleSpectralArrowClick(HumanEntity player) {
        Bukkit.getScheduler().runTaskAsynchronously(creator.getPlugin(), creator.showDashboardView(player, 1));
    }

    private void handleArrowClick(HumanEntity player, InventoryView view, ItemStack arrow) {
        var parts = view.getTitle().split("[()/]");
        long currentPage = Long.parseLong(parts[1]);
        long totalPages = Long.parseLong(parts[2]);

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

        Bukkit.getScheduler().runTaskAsynchronously(creator.getPlugin(),
                creator.showSupervisedTasksView(player, currentPage));
    }
}
