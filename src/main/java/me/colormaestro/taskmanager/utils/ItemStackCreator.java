package me.colormaestro.taskmanager.utils;

import me.colormaestro.taskmanager.enums.TaskStatus;
import me.colormaestro.taskmanager.model.Task;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ItemStackCreator {
    private final static int LORE_WIDTH_LIMIT = 40;
    private static final int PREVIOUS_PAGE_POSITION = 45;
    private static final int NEXT_PAGE_POSITION = 53;

    public static ItemStack createMemberStack(String uuid, String ign, int doing, int finished, int approved) {
        ItemStack is = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta skullMeta = (SkullMeta) is.getItemMeta();
        if (skullMeta == null) {
            return null;
        }
        OfflinePlayer op = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
        skullMeta.setOwningPlayer(op);
        skullMeta.setDisplayName(ChatColor.BLUE + "" + ChatColor.BOLD + ign);
        skullMeta.setLore(createHeadStatsLore(doing, finished, approved));
        is.setItemMeta(skullMeta);
        return is;
    }

    private static List<String> createHeadStatsLore(int doing, int finished, int approved) {
        List<String> result = new ArrayList<>();

        result.add(ChatColor.GRAY + "Opened: " + ChatColor.GOLD + doing);
        result.add(ChatColor.GRAY + "Finished: " + ChatColor.GREEN + finished);
        result.add(ChatColor.GRAY + "Approved: " + ChatColor.AQUA + approved);

        return result;
    }

    public static ItemStack createTaskStack(
            Integer taskId,
            String title,
            String description,
            TaskStatus status,
            String assigneeIgn
    ) {
        Material material = Material.ORANGE_CONCRETE;
        switch (status) {
            case FINISHED -> material = Material.LIME_CONCRETE;
            case APPROVED -> material = Material.LIGHT_BLUE_CONCRETE;
        }
        ItemStack is = new ItemStack(material, 1);
        ItemMeta itemMeta = is.getItemMeta();
        if (itemMeta == null) {
            return null;
        }
        itemMeta.setDisplayName(ChatColor.BLUE + "" + ChatColor.BOLD + title + " " + ChatColor.DARK_GRAY + "#" + taskId);

        List<String> taskDescriptionLore = createTaskDescriptionLore(description);
        if (assigneeIgn != null) {
            taskDescriptionLore.add(0, ChatColor.GRAY + "Assignee: " + ChatColor.GOLD + assigneeIgn);
        }

        itemMeta.setLore(taskDescriptionLore);
        is.setItemMeta(itemMeta);
        return is;
    }

    private static List<String> createTaskDescriptionLore(String input) {
        List<String> result = new ArrayList<>();

        String[] words = input.split("\\s+");
        StringBuilder currentString = new StringBuilder();

        for (String word : words) {
            if (currentString.length() + word.length() + 1 <= LORE_WIDTH_LIMIT) {
                if (currentString.length() > 0) {
                    currentString.append(" ");
                }
                currentString.append(word);
            } else {
                result.add(ChatColor.GRAY + currentString.toString());
                currentString = new StringBuilder(word);
            }
        }

        if (currentString.length() > 0) {
            result.add(ChatColor.GRAY + currentString.toString());
        }

        return result;
    }

    public static void supplyInventoryWithPaginationArrows(Inventory inventory) {
        ItemStack stack = new ItemStack(Material.ARROW, 1);
        ItemMeta meta = stack.getItemMeta();
        assert meta != null;
        meta.setDisplayName("Previous page");
        stack.setItemMeta(meta);
        inventory.setItem(PREVIOUS_PAGE_POSITION, stack);

        stack = new ItemStack(Material.ARROW, 1);
        meta = stack.getItemMeta();
        assert meta != null;
        meta.setDisplayName("Next page");
        stack.setItemMeta(meta);
        inventory.setItem(NEXT_PAGE_POSITION, stack);
    }
}
