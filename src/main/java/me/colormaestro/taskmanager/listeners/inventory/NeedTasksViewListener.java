package me.colormaestro.taskmanager.listeners.inventory;

import me.colormaestro.taskmanager.utils.DataContainerKeys;
import me.colormaestro.taskmanager.utils.Directives;
import me.colormaestro.taskmanager.utils.RunnablesCreator;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

public class NeedTasksViewListener extends InventoryListener {

    public NeedTasksViewListener(RunnablesCreator creator) {
        super(creator, Directives.NEED_TASKS);
    }

    @EventHandler
    void onClick(InventoryClickEvent event) {
        onInventoryClick(event);
    }

    @Override
    void handleEvent(HumanEntity player, ItemStack itemStack) {
        switch (itemStack.getType()) {
            case PLAYER_HEAD -> handlePlayerHeadClick(player, itemStack.getItemMeta());
            case ARROW -> handleArrowClick(player, itemStack.getItemMeta());
            case SPECTRAL_ARROW -> Bukkit.getScheduler()
                    .runTaskAsynchronously(creator.getPlugin(), creator.showDashboardView(player, 1));
        }
    }

    private void handlePlayerHeadClick(HumanEntity player, PersistentDataHolder holder) {
        String ign = extractPersistentValue(holder, DataContainerKeys.MEMBER_NAME, PersistentDataType.STRING);
        Bukkit.getScheduler().runTaskAsynchronously(creator.getPlugin(), creator.showActiveTasksView(player, ign, 1));
    }

    private void handleArrowClick(HumanEntity player, PersistentDataHolder holder) {
        Bukkit.getScheduler().runTaskAsynchronously(creator.getPlugin(),
                creator.showNeedTasksView(player, determineNextPage(holder)));
    }
}
