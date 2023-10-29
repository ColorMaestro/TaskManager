package me.colormaestro.taskmanager.utils;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class SkullMetaBuilder extends ItemMetaBuilder {
    private final SkullMeta skullMeta;

    public SkullMetaBuilder() {
        super(Material.PLAYER_HEAD);
        skullMeta = (SkullMeta) itemMeta;
    }

    public SkullMetaBuilder setOwningPlayer(OfflinePlayer offlinePlayer) {
        skullMeta.setOwningPlayer(offlinePlayer);
        return this;
    }

    @Override
    public ItemMeta build() {
        return skullMeta;
    }
}
