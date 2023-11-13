package me.colormaestro.taskmanager.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class ItemMetaBuilder {
    protected final ItemMeta itemMeta;

    public ItemMetaBuilder() {
        this(Material.WHITE_CONCRETE);
    }

    protected ItemMetaBuilder(Material material) {
        itemMeta = Bukkit.getItemFactory().getItemMeta(material);
    }

    public ItemMetaBuilder setDisplayName(String displayName) {
        itemMeta.setDisplayName(displayName);
        return this;
    }

    public ItemMetaBuilder setLore(List<String> lore) {
        itemMeta.setLore(lore);
        return this;
    }

    public <T, Z> ItemMetaBuilder setPersistentData(NamespacedKey key, PersistentDataType<T, Z> type, Z value) {
        itemMeta.getPersistentDataContainer().set(key, type, value);
        return this;
    }

    public ItemMeta build() {
        return itemMeta;
    }
}
