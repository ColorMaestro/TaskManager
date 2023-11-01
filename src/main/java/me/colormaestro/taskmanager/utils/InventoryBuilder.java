package me.colormaestro.taskmanager.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class InventoryBuilder {
    private final Inventory inventory;
    private static final int INVENTORY_SIZE = 54;
    private static final int SIXTH_ROW_FIRST_POSITION = 45;
    private static final int SIXTH_ROW_LAST_POSITION = 53;

    public InventoryBuilder(HumanEntity player, String title) {
        inventory = Bukkit.createInventory(player, INVENTORY_SIZE, title);
    }

    public InventoryBuilder addItemStack(int position, ItemStack stack) {
        inventory.setItem(position, stack);
        return this;
    }

    public InventoryBuilder addItemStack(int position, Material material, String displayName) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = new ItemMetaBuilder().setDisplayName(displayName).build();
        stack.setItemMeta(meta);
        inventory.setItem(position, stack);
        return this;
    }

    public InventoryBuilder addPaginationArrows() {
        return addItemStack(SIXTH_ROW_FIRST_POSITION, Material.ARROW, "Previous page")
                .addItemStack(SIXTH_ROW_LAST_POSITION, Material.ARROW, "Next page");
    }

    public Inventory build() {
        return inventory;
    }
}
