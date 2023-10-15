package me.colormaestro.taskmanager.listeners;

import me.colormaestro.taskmanager.utils.Directives;
import me.colormaestro.taskmanager.utils.RunnablesCreator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class ActiveTasksViewListener implements Listener {
    private final RunnablesCreator creator;

    public ActiveTasksViewListener(RunnablesCreator creator) {
        this.creator = creator;
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
                case ARROW -> handleArrowClick(player, event.getView(), event.getCurrentItem());
            }
        }
    }

    private void handleConcreteClick(HumanEntity player, ItemStack headStack) {
        String taskId = headStack.getItemMeta().getDisplayName().split("#")[1];
        Bukkit.getScheduler().runTaskAsynchronously(creator.getPlugin(), creator.teleportPlayerToTask(player, taskId));
    }

    private void handleShowApprovedTasksClick(HumanEntity player, ItemStack concreteStack) {
        String adjective = concreteStack.getItemMeta().getDisplayName().split(" ")[1];
        String ign = adjective.split("'")[0];
        Bukkit.getScheduler().runTaskAsynchronously(creator.getPlugin(), creator.showApprovedTasksView(player, ign, 1));
    }

    private void handleSpectralArrowClick(HumanEntity player) {
        Bukkit.getScheduler().runTaskAsynchronously(creator.getPlugin(), creator.showDashboardView(player, 1));
    }

    private void handleArrowClick(HumanEntity player, InventoryView view, ItemStack arrow) {
        var parts = view.getTitle().replaceFirst(ChatColor.BLUE + "" + ChatColor.BOLD, "").split("['()/]");
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

        Bukkit.getScheduler().runTaskAsynchronously(creator.getPlugin(),
                creator.showActiveTasksView(player, ign, currentPage));
    }
}
