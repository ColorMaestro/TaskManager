package me.colormaestro.taskmanager.utils;

import me.colormaestro.taskmanager.enums.TaskStatus;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ItemStackCreator {
    private final static int LORE_WIDTH_LIMIT = 40;

    public static ItemStack createMemberStack(String uuid, String ign, int doing, int finished, int approved, Date lastLogin) {
        ItemStack is = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta skullMeta = (SkullMeta) is.getItemMeta();
        if (skullMeta == null) {
            return null;
        }
        OfflinePlayer op = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
        skullMeta.setOwningPlayer(op);
        skullMeta.setDisplayName(ChatColor.BLUE + "" + ChatColor.BOLD + ign);

        List<String> lore = createHeadStatsLore(doing, finished, approved);

        LocalDate currentDate = LocalDate.now();
        LocalDate sqlLocalDate = lastLogin.toLocalDate();
        long daysDelta = ChronoUnit.DAYS.between(sqlLocalDate, currentDate);

        lore.add("");
        lore.add(ChatColor.GRAY + "Last online: " + ChatColor.WHITE + daysDelta + " day(s) ago");

        skullMeta.setLore(lore);
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
            case PREPARED -> material = Material.LIGHT_GRAY_CONCRETE;
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
                if (!currentString.isEmpty()) {
                    currentString.append(" ");
                }
                currentString.append(word);
            } else {
                result.add(ChatColor.GRAY + currentString.toString());
                currentString = new StringBuilder(word);
            }
        }

        if (!currentString.isEmpty()) {
            result.add(ChatColor.GRAY + currentString.toString());
        }

        return result;
    }

    /**
     * Creates ItemStack of Material.WRITABLE_BOOK for creating new task
     *
     * @param ign name of member for which to create task, for given name created task will be directly assigned
     *            to the member (in {@link me.colormaestro.taskmanager.enums.TaskStatus#DOING} state), otherwise null
     *            value causes that task will be only prepared (in {@link me.colormaestro.taskmanager.enums.TaskStatus#PREPARED} state)
     * @param description instructions what to do in the task
     * @return ItemStack for creating the task
     */
    public static ItemStack createAssignmentBook(String ign, String description) {
        ItemStack book = new ItemStack(Material.WRITABLE_BOOK);
        BookMeta bookMeta = (BookMeta) book.getItemMeta();

        BaseComponent[] page = new ComponentBuilder("Instructions:")
                .color(net.md_5.bungee.api.ChatColor.BLUE).bold(true)
                // new line must be in this block, otherwise color continues, seems like a bug in spigot
                .append("""

                        1) Only the second page of this book serves as task description for player what to do in this task.
                        """)
                .color(net.md_5.bungee.api.ChatColor.RESET).bold(false)
                .append("2) Book title serves as headline for the task - this will be displayed at the hologram.\n")
                .append("3) Tasks is created immediately after you sign the book.\n")
                .create();

        BaseComponent[] page2 = new ComponentBuilder(description).create();

        bookMeta.spigot().addPage(page);
        bookMeta.spigot().addPage(page2);

        if (ign != null) {
            bookMeta.setDisplayName(ChatColor.GOLD + "Assignment book for " + ign);
            bookMeta.setLore(new ArrayList<>(Arrays.asList(Directives.CREATE_TASK, ign)));
        } else {
            bookMeta.setDisplayName(ChatColor.GRAY + "Book for creation of prepared task");
            bookMeta.setLore(new ArrayList<>(List.of(Directives.PREPARE_TASK)));
        }

        book.setItemMeta(bookMeta);
        return book;
    }
}
