package me.colormaestro.taskmanager.listeners.inventory;

import me.colormaestro.taskmanager.utils.DataContainerKeys;
import me.colormaestro.taskmanager.utils.Directives;
import me.colormaestro.taskmanager.utils.ItemStackCreator;
import me.colormaestro.taskmanager.utils.RunnablesCreator;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

public class ActiveTasksViewListener extends InventoryListener {

    private final ItemStackCreator stackCreator;

    public ActiveTasksViewListener(RunnablesCreator creator) {
        super(creator, Directives.ACTIVE_TASKS);
        stackCreator = new ItemStackCreator(creator.getPlugin());
    }

    @EventHandler
    void onClick(InventoryClickEvent event) {
        onInventoryClick(event);
    }

    @Override
    void handleEvent(HumanEntity player, ItemStack itemStack, ClickType clickType) {
        switch (itemStack.getType()) {
            case ORANGE_CONCRETE, LIME_CONCRETE -> handleConcreteClick(player, itemStack.getItemMeta());
            case LIGHT_BLUE_CONCRETE -> handleShowApprovedTasksClick(player, itemStack.getItemMeta());
            case WRITABLE_BOOK -> handleBookCLick(player);
            case SPECTRAL_ARROW -> scheduler
                    .runTaskAsynchronously(creator.getPlugin(), creator.showDashboardView(player, 1));
            case ARROW -> handleArrowClick(player, itemStack.getItemMeta());
        }
    }

    private void handleBookCLick(HumanEntity player) {
        ItemStack book = stackCreator.createAssignmentBook(player.getName(), "");
        player.closeInventory();
        player.getInventory().addItem(book);
    }

    private void handleConcreteClick(HumanEntity player, PersistentDataHolder holder) {
        int taskId = extractPersistentValue(holder, DataContainerKeys.TASK_ID, PersistentDataType.INTEGER);
        scheduler.runTaskAsynchronously(creator.getPlugin(), creator.teleportPlayerToTask(player, taskId));
    }

    private void handleShowApprovedTasksClick(HumanEntity player, PersistentDataHolder holder) {
        String ign = extractPersistentValue(holder, DataContainerKeys.MEMBER_NAME, PersistentDataType.STRING);
        scheduler.runTaskAsynchronously(creator.getPlugin(), creator.showApprovedTasksView(player, ign, 1));
    }

    private void handleArrowClick(HumanEntity player, PersistentDataHolder holder) {
        String ign = extractPersistentValue(holder, DataContainerKeys.MEMBER_NAME, PersistentDataType.STRING);
        int subsequentPage = determineNextPage(holder);

        scheduler.runTaskAsynchronously(creator.getPlugin(), creator.showActiveTasksView(player, ign, subsequentPage));
    }
}
