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

public class SelectMemberListener extends InventoryListener {

    public SelectMemberListener(RunnablesCreator creator) {
        super(creator, Directives.SELECT_MEMBER);
    }

    @EventHandler
    void onClick(InventoryClickEvent event) {
        onInventoryClick(event);
    }

    @Override
    void handleEvent(HumanEntity player, ItemStack itemStack, ClickType clickType) {
        switch (itemStack.getType()) {
            case PLAYER_HEAD -> handlePlayerHeadClick(player, itemStack.getItemMeta());
            case SPECTRAL_ARROW -> scheduler
                    .runTaskAsynchronously(creator.getPlugin(), creator.showPreparedTasksView(player, 1));
            case ARROW -> handleArrowClick(player, itemStack.getItemMeta());
        }
    }

    private void handlePlayerHeadClick(HumanEntity player, PersistentDataHolder holder) {
        String ign = extractPersistentValue(holder, DataContainerKeys.MEMBER_NAME, PersistentDataType.STRING);
        int taskId = extractPersistentValue(player, DataContainerKeys.TASK_ID, PersistentDataType.INTEGER);
        scheduler.runTaskAsynchronously(creator.getPlugin(), creator.assignTask(ign, player, taskId));
        player.closeInventory();
    }

    private void handleArrowClick(HumanEntity player, PersistentDataHolder holder) {
        int taskId = extractPersistentValue(player, DataContainerKeys.TASK_ID, PersistentDataType.INTEGER);
        scheduler.runTaskAsynchronously(creator.getPlugin(),
                creator.showAssignTasksView(player, taskId, determineNextPage(holder)));
    }
}
