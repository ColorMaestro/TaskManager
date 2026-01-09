package me.colormaestro.taskmanager.listeners.inventory;

import me.colormaestro.taskmanager.scheduler.Scheduler;
import me.colormaestro.taskmanager.utils.DataContainerKeys;
import me.colormaestro.taskmanager.utils.Directives;
import me.colormaestro.taskmanager.utils.ItemStackCreator;
import me.colormaestro.taskmanager.utils.RunnablesCreator;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

public class DashboardViewListener extends InventoryListener implements Listener {

    private final ItemStackCreator stackCreator;

    public DashboardViewListener(Scheduler scheduler, RunnablesCreator creator) {
        super(scheduler, creator, Directives.DASHBOARD);
        stackCreator = new ItemStackCreator(creator.getPlugin());
    }

    @EventHandler
    void onClick(InventoryClickEvent event) {
        onInventoryClick(event);
    }

    @Override
    void handleEvent(HumanEntity player, ItemStack itemStack, ClickType clickType) {
        switch (itemStack.getType()) {
            case PLAYER_HEAD -> handlePlayerHeadClick(player, itemStack.getItemMeta(), clickType.isLeftClick());
            case ENDER_EYE -> scheduler
                    .runTaskAsynchronously(creator.showSupervisedTasksView(player, 1));
            case ARROW -> handleArrowClick(player, itemStack.getItemMeta());
            case LIGHT_GRAY_CONCRETE -> scheduler
                    .runTaskAsynchronously(creator.showPreparedTasksView(player, 1));
            case CLOCK -> scheduler
                    .runTaskAsynchronously(creator.showIdleTasksView(player, 1));
            case PAPER -> scheduler
                    .runTaskAsynchronously(creator.showNeedTasksView(player, 0, 1));
        }
    }

    private void handlePlayerHeadClick(HumanEntity player, PersistentDataHolder holder, boolean isLeftClick) {
        String ign = extractPersistentValue(holder, DataContainerKeys.MEMBER_NAME, PersistentDataType.STRING);
        if (isLeftClick) {
            scheduler.runTaskAsynchronously(creator.showActiveTasksView(player, ign, 1));
        } else {
            ItemStack book = stackCreator.createAssignmentBook(ign, "");
            player.closeInventory();
            player.getInventory().addItem(book);
        }
    }

    private void handleArrowClick(HumanEntity player, PersistentDataHolder holder) {
        scheduler.runTaskAsynchronously(creator.showDashboardView(player, determineNextPage(holder)));
    }
}
