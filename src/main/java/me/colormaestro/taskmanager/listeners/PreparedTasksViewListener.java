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

public class PreparedTasksViewListener extends InventoryListener {

    public PreparedTasksViewListener(RunnablesCreator creator) {
        super(creator, Directives.PREPARED_TASKS);
    }

    @EventHandler
    void onClick(InventoryClickEvent event) {
        onInventoryClick(event);
    }

    @Override
    void handleEvent(HumanEntity player, ItemStack itemStack) {
        switch (itemStack.getType()) {
            case LIGHT_GRAY_CONCRETE -> handleConcreteClick(player, itemStack.getItemMeta());
            case SPECTRAL_ARROW -> handleSpectralArrowClick(player);
            case ARROW -> handleArrowClick(player, itemStack.getItemMeta());
        }
    }

    private void handleConcreteClick(HumanEntity player, PersistentDataHolder holder) {
        int taskId = extractPersistentValue(holder, DataContainerKeys.TASK_ID, PersistentDataType.INTEGER);
        Bukkit.getScheduler().runTaskAsynchronously(creator.getPlugin(), creator.teleportPlayerToTask(player, taskId));
    }

    private void handleSpectralArrowClick(HumanEntity player) {
        Bukkit.getScheduler().runTaskAsynchronously(creator.getPlugin(), creator.showDashboardView(player, 1));
    }

    private void handleArrowClick(HumanEntity player, PersistentDataHolder holder) {
        int currentPage = extractPersistentValue(holder, DataContainerKeys.CURRENT_PAGE, PersistentDataType.INTEGER);
        int totalPages = extractPersistentValue(holder, DataContainerKeys.TOTAL_PAGES, PersistentDataType.INTEGER);

        if (hasPersistentValue(holder, DataContainerKeys.TURN_NEXT_PAGE, PersistentDataType.STRING)) {
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
                creator.showPreparedTasksView(player, currentPage));
    }
}
