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

public class NeedTasksViewListener extends InventoryListener {
    private static final int MAX_LIMIT = 10;

    public NeedTasksViewListener(RunnablesCreator creator) {
        super(creator, Directives.NEED_TASKS);
    }

    @EventHandler
    void onClick(InventoryClickEvent event) {
        onInventoryClick(event);
    }

    @Override
    void handleEvent(HumanEntity player, ItemStack itemStack, ClickType clickType) {
        switch (itemStack.getType()) {
            case PLAYER_HEAD -> handlePlayerHeadClick(player, itemStack.getItemMeta());
            case SMALL_AMETHYST_BUD -> decreaseLimit(player, itemStack.getItemMeta());
            case AMETHYST_CLUSTER -> increaseLimit(player, itemStack.getItemMeta());
            case ARROW -> handleArrowClick(player, itemStack.getItemMeta());
            case SPECTRAL_ARROW -> scheduler
                    .runTaskAsynchronously(creator.getPlugin(), creator.showDashboardView(player, 1));
        }
    }

    private void handlePlayerHeadClick(HumanEntity player, PersistentDataHolder holder) {
        String ign = extractPersistentValue(holder, DataContainerKeys.MEMBER_NAME, PersistentDataType.STRING);
        scheduler.runTaskAsynchronously(creator.getPlugin(), creator.showActiveTasksView(player, ign, 1));
    }

    private void handleArrowClick(HumanEntity player, PersistentDataHolder holder) {
        int currentLimit = extractPersistentValue(player, DataContainerKeys.CURRENT_LIMIT, PersistentDataType.INTEGER);
        scheduler.runTaskAsynchronously(creator.getPlugin(),
                creator.showNeedTasksView(player, currentLimit, determineNextPage(holder)));
    }

    private void decreaseLimit(HumanEntity player, PersistentDataHolder holder) {
        int currentLimit = extractPersistentValue(holder, DataContainerKeys.CURRENT_LIMIT, PersistentDataType.INTEGER);

        currentLimit--;

        if (currentLimit < 0)
            currentLimit = 0;

        scheduler.runTaskAsynchronously(creator.getPlugin(), creator.showNeedTasksView(player, currentLimit, 1));
    }

    private void increaseLimit(HumanEntity player, PersistentDataHolder holder) {
        int currentLimit = extractPersistentValue(holder, DataContainerKeys.CURRENT_LIMIT, PersistentDataType.INTEGER);

        currentLimit++;

        if (currentLimit > MAX_LIMIT)
            currentLimit = MAX_LIMIT;

        scheduler.runTaskAsynchronously(creator.getPlugin(), creator.showNeedTasksView(player, currentLimit, 1));
    }
}
