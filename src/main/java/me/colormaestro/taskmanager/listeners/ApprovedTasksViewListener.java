package me.colormaestro.taskmanager.listeners;

import me.colormaestro.taskmanager.utils.DataContainerKeys;
import me.colormaestro.taskmanager.utils.Directives;
import me.colormaestro.taskmanager.utils.RunnablesCreator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

public class ApprovedTasksViewListener extends InventoryListener {

    public ApprovedTasksViewListener(RunnablesCreator creator) {
        super(creator, Directives.APPROVED_TASKS);
    }

    @Override
    @EventHandler
    void onInventoryClick(InventoryClickEvent event) {
        super.onInventoryClick(event);
    }

    @Override
    void handleEvent(InventoryClickEvent event) {
        HumanEntity player = event.getView().getPlayer();
        switch (event.getCurrentItem().getType()) {
            case LIGHT_BLUE_CONCRETE -> handleConcreteClick(player, event.getCurrentItem());
            case SPECTRAL_ARROW -> handleSpectralArrowClick(player, event.getCurrentItem());
            case ARROW -> handleArrowClick(player, event.getView(), event.getCurrentItem());
        }
    }

    private void handleConcreteClick(HumanEntity player, ItemStack headStack) {
        int taskId = Objects.requireNonNull(headStack.getItemMeta()).getPersistentDataContainer()
                .get(new NamespacedKey(creator.getPlugin(), DataContainerKeys.TASK_ID), PersistentDataType.INTEGER);
        Bukkit.getScheduler().runTaskAsynchronously(creator.getPlugin(), creator.teleportPlayerToTask(player, taskId));
    }

    private void handleSpectralArrowClick(HumanEntity player, ItemStack arrow) {
        String ign = Objects.requireNonNull(arrow.getItemMeta()).getPersistentDataContainer()
                .get(new NamespacedKey(creator.getPlugin(), DataContainerKeys.MEMBER_NAME), PersistentDataType.STRING);
        Bukkit.getScheduler().runTaskAsynchronously(creator.getPlugin(), creator.showActiveTasksView(player, ign, 1));
    }

    private void handleArrowClick(HumanEntity player, InventoryView view, ItemStack arrow) {
        var parts = view.getTitle().replaceFirst(ChatColor.DARK_AQUA + "" + ChatColor.BOLD, "").split("['()/]");
        String ign = parts[0];
        long currentPage = Long.parseLong(parts[2]);
        long totalPages = Long.parseLong(parts[3]);

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

        Bukkit.getScheduler().runTaskAsynchronously(creator.getPlugin(),
                creator.showApprovedTasksView(player, ign, currentPage));
    }
}
