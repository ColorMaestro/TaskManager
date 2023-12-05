package me.colormaestro.taskmanager.listeners.inventory;

import me.colormaestro.taskmanager.utils.DataContainerKeys;
import me.colormaestro.taskmanager.utils.Directives;
import me.colormaestro.taskmanager.utils.RunnablesCreator;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

public class SupervisedTasksViewListener extends InventoryListener {

    public SupervisedTasksViewListener(RunnablesCreator creator) {
        super(creator, Directives.SUPERVISED_TASKS);
    }

    @EventHandler
    void onClick(InventoryClickEvent event) {
        onInventoryClick(event);
    }

    @Override
    void handleEvent(HumanEntity player, ItemStack itemStack, ClickType clickType) {
        switch (itemStack.getType()) {
            case ORANGE_CONCRETE, LIME_CONCRETE -> handleConcreteClick(player, itemStack.getItemMeta());
            case SPECTRAL_ARROW -> Bukkit.getScheduler()
                    .runTaskAsynchronously(creator.getPlugin(), creator.showDashboardView(player, 1));
            case ARROW -> handleArrowClick(player, itemStack.getItemMeta());
        }
    }

    private void handleConcreteClick(HumanEntity player, PersistentDataHolder holder) {
        int taskId = extractPersistentValue(holder, DataContainerKeys.TASK_ID, PersistentDataType.INTEGER);
        Bukkit.getScheduler().runTaskAsynchronously(creator.getPlugin(), creator.teleportPlayerToTask(player, taskId));
    }

    private void handleArrowClick(HumanEntity player, PersistentDataHolder holder) {
        Bukkit.getScheduler().runTaskAsynchronously(creator.getPlugin(),
                creator.showSupervisedTasksView(player, determineNextPage(holder)));
    }
}
