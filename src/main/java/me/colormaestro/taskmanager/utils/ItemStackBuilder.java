package me.colormaestro.taskmanager.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ItemStackBuilder {
    public static ItemStack buildMemberStack(String uuid, String ign, int doing, int finished, int approved) {
        ItemStack is = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta skullMeta = (SkullMeta) is.getItemMeta();
        if (skullMeta == null) {
            return null;
        }
        OfflinePlayer op = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
        skullMeta.setOwningPlayer(op);
        skullMeta.setDisplayName(ChatColor.BLUE + "" + ChatColor.BOLD + ign);
        skullMeta.setLore(buildHeadStatsLore(doing, finished, approved));
        is.setItemMeta(skullMeta);
        return is;
    }

    private static List<String> buildHeadStatsLore(int doing, int finished, int approved) {
        List<String> result = new ArrayList<>();

        result.add(ChatColor.GRAY + "Opened: " + ChatColor.GOLD + doing);
        result.add(ChatColor.GRAY + "Finished: " + ChatColor.GREEN + finished);
        result.add(ChatColor.GRAY + "Approved: " + ChatColor.AQUA + approved);

        return result;
    }
}
