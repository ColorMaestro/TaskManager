package me.colormaestro.taskmanager.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryClickListener implements Listener {

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (event.getView().getTitle().contains("*-*")) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null) {
                return;
            }

            HumanEntity player = event.getView().getPlayer();
            switch (event.getCurrentItem().getType()) {
                case PLAYER_HEAD -> handlePlayerHeadClick(player, event.getCurrentItem());
                case ARROW -> handleArrowClick();
            }
        }
    }

    private void handlePlayerHeadClick(HumanEntity player, ItemStack stack) {
        String ign = stack.getItemMeta().getDisplayName().replaceFirst(ChatColor.BLUE + "" + ChatColor.BOLD, "");
        player.sendMessage(ign);
        player.openInventory(Bukkit.createInventory(player, 9, "inventoryTitle"));
    }

    private void handleArrowClick() {

    }
}
