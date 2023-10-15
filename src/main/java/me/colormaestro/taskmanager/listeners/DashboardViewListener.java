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

public class DashboardViewListener implements Listener {
    private final RunnablesCreator creator;

    public DashboardViewListener(RunnablesCreator creator) {
        this.creator = creator;
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
                case ENDER_EYE -> handleEyeClick(player);
                case ARROW -> handleArrowClick(player, event.getView(), event.getCurrentItem());
                case LIGHT_GRAY_CONCRETE -> handleConcreteClick(player);
                case CLOCK -> handleClockClick(player);
            }
        }
    }

    private void handlePlayerHeadClick(HumanEntity player, ItemStack headStack) {
        String ign = headStack.getItemMeta().getDisplayName().replaceFirst(ChatColor.BLUE + "" + ChatColor.BOLD, "");
        Bukkit.getScheduler().runTaskAsynchronously(creator.getPlugin(), creator.showActiveTasksView(player, ign, 1));
    }

    private void handleEyeClick(HumanEntity player) {
        Bukkit.getScheduler().runTaskAsynchronously(creator.getPlugin(), creator.showSupervisedTasksView(player, 1));
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

        Bukkit.getScheduler().runTaskAsynchronously(creator.getPlugin(), creator.showDashboardView(player, currentPage));
    }

    private void handleConcreteClick(HumanEntity player) {
        Bukkit.getScheduler().runTaskAsynchronously(creator.getPlugin(), creator.showPreparedTasksView(player, 1));
    }

    private void handleClockClick(HumanEntity player) {
        Bukkit.getScheduler().runTaskAsynchronously(creator.getPlugin(), creator.showIdleTasksView(player, 1));
    }
}
