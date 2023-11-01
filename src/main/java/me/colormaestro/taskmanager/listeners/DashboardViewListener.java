package me.colormaestro.taskmanager.listeners;

import me.colormaestro.taskmanager.utils.DataContainerKeys;
import me.colormaestro.taskmanager.utils.Directives;
import me.colormaestro.taskmanager.utils.RunnablesCreator;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

public class DashboardViewListener extends InventoryListener implements Listener {

    public DashboardViewListener(RunnablesCreator creator) {
        super(creator, Directives.DASHBOARD);
    }

    @EventHandler
    void onClick(InventoryClickEvent event) {
        onInventoryClick(event);
    }

    @Override
    void handleEvent(HumanEntity player, ItemStack itemStack) {
        switch (itemStack.getType()) {
            case PLAYER_HEAD -> handlePlayerHeadClick(player, itemStack.getItemMeta());
            case ENDER_EYE -> handleEyeClick(player);
            case ARROW -> handleArrowClick(player, itemStack.getItemMeta());
            case LIGHT_GRAY_CONCRETE -> handleConcreteClick(player);
            case CLOCK -> handleClockClick(player);
        }
    }

    private void handlePlayerHeadClick(HumanEntity player, PersistentDataHolder holder) {
        String ign = extractPersistentValue(holder, DataContainerKeys.MEMBER_NAME, PersistentDataType.STRING);
        Bukkit.getScheduler().runTaskAsynchronously(creator.getPlugin(), creator.showActiveTasksView(player, ign, 1));
    }

    private void handleEyeClick(HumanEntity player) {
        Bukkit.getScheduler().runTaskAsynchronously(creator.getPlugin(), creator.showSupervisedTasksView(player, 1));
    }

    private void handleArrowClick(HumanEntity player, PersistentDataHolder holder) {
        Bukkit.getScheduler().runTaskAsynchronously(creator.getPlugin(),
                creator.showDashboardView(player, determineNextPage(holder)));
    }

    private void handleConcreteClick(HumanEntity player) {
        Bukkit.getScheduler().runTaskAsynchronously(creator.getPlugin(), creator.showPreparedTasksView(player, 1));
    }

    private void handleClockClick(HumanEntity player) {
        Bukkit.getScheduler().runTaskAsynchronously(creator.getPlugin(), creator.showIdleTasksView(player, 1));
    }
}
