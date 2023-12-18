package me.colormaestro.taskmanager.enums;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public enum TaskStatus {
    DOING(ChatColor.GOLD, Material.ORANGE_CONCRETE),
    FINISHED(ChatColor.GREEN, Material.LIME_CONCRETE),
    APPROVED(ChatColor.AQUA, Material.LIGHT_BLUE_CONCRETE),
    PREPARED(ChatColor.GRAY, Material.LIGHT_GRAY_CONCRETE);

    public final ChatColor color;
    public final Material material;

    TaskStatus(ChatColor color, Material material) {
        this.color = color;
        this.material = material;
    }
}
