package me.colormaestro.taskmanager.listeners;

import me.colormaestro.taskmanager.utils.DataContainerKeys;
import me.colormaestro.taskmanager.utils.Directives;
import me.colormaestro.taskmanager.utils.RunnablesCreator;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

public class IdleTaskViewListener extends InventoryListener {

    public IdleTaskViewListener(RunnablesCreator creator) {
        super(creator, Directives.IDLE_TASKS);
    }

    @EventHandler
    void onClick(InventoryClickEvent event) {
        onInventoryClick(event);
    }

    @Override
    void handleEvent(InventoryClickEvent event) {
        HumanEntity player = event.getView().getPlayer();
        switch (event.getCurrentItem().getType()) {
            case ORANGE_CONCRETE -> handleConcreteClick(player, event.getCurrentItem().getItemMeta());
            case SPECTRAL_ARROW -> handleSpectralArrowClick(player);
            case ARROW -> handleArrowClick(player, event.getCurrentItem());
        }
    }

    private void handleConcreteClick(HumanEntity player, PersistentDataHolder holder) {
        int taskId = extractPersistentValue(holder, DataContainerKeys.TASK_ID, PersistentDataType.INTEGER);
        Bukkit.getScheduler().runTaskAsynchronously(creator.getPlugin(), creator.teleportPlayerToTask(player, taskId));
    }

    private void handleSpectralArrowClick(HumanEntity player) {
        Bukkit.getScheduler().runTaskAsynchronously(creator.getPlugin(), creator.showDashboardView(player, 1));
    }

    private void handleArrowClick(HumanEntity player, ItemStack arrow) {
        int currentPage = extractPersistentValue(arrow.getItemMeta(), DataContainerKeys.CURRENT_PAGE, PersistentDataType.INTEGER);
        int totalPages = extractPersistentValue(arrow.getItemMeta(), DataContainerKeys.TOTAL_PAGES, PersistentDataType.INTEGER);

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

        Bukkit.getScheduler().runTaskAsynchronously(creator.getPlugin(), creator.showIdleTasksView(player, currentPage));
    }
}
