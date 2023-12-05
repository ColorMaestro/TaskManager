package me.colormaestro.taskmanager.utils;

import me.colormaestro.taskmanager.enums.TaskStatus;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ItemStackCreator {
    private final static int LORE_WIDTH_LIMIT = 40;
    private final Plugin plugin;

    public ItemStackCreator(Plugin plugin) {
        this.plugin = plugin;
    }

    public ItemStack createMemberStack(String uuid, String ign, int doing, int finished, int approved, Date lastLogin) {
        ItemStack stack = new ItemStack(Material.PLAYER_HEAD);

        ItemMeta meta = new SkullMetaBuilder()
                .setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(uuid)))
                .setDisplayName(ChatColor.BLUE + "" + ChatColor.BOLD + ign)
                .setLore(createMemberStackLore(doing, finished, approved, lastLogin))
                .setPersistentData(new NamespacedKey(plugin, DataContainerKeys.MEMBER_NAME), PersistentDataType.STRING, ign)
                .build();

        stack.setItemMeta(meta);
        return stack;
    }

    private List<String> createMemberStackLore(int doing, int finished, int approved, Date lastLogin) {
        List<String> lore = new ArrayList<>();

        lore.add(ChatColor.GRAY + "In progress: " + ChatColor.GOLD + doing);
        lore.add(ChatColor.GRAY + "Finished: " + ChatColor.GREEN + finished);
        lore.add(ChatColor.GRAY + "Approved: " + ChatColor.AQUA + approved);

        LocalDate currentDate = LocalDate.now();
        LocalDate sqlLocalDate = lastLogin.toLocalDate();
        long daysDelta = ChronoUnit.DAYS.between(sqlLocalDate, currentDate);

        lore.add(ChatColor.GRAY + "Last online: " + ChatColor.WHITE + daysDelta + " day(s) ago");

        lore.add("");
        lore.add(ChatColor.YELLOW + "➜ Left-Click to view active tasks");
        lore.add(ChatColor.AQUA + "➜ Right-Click to add new task");

        return lore;
    }

    public ItemStack createNeedTasksStack(String uuid, String ign, int doing) {
        ItemStack stack = new ItemStack(Material.PLAYER_HEAD);

        ItemMeta meta = new SkullMetaBuilder()
                .setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(uuid)))
                .setDisplayName(ChatColor.BLUE + "" + ChatColor.BOLD + ign)
                .setLore(List.of(ChatColor.WHITE + "" + doing + " tasks in progress"))
                .setPersistentData(new NamespacedKey(plugin, DataContainerKeys.MEMBER_NAME), PersistentDataType.STRING, ign)
                .build();

        stack.setItemMeta(meta);
        return stack;
    }

    public ItemStack createBasicTaskStack(Integer taskId, String title, String description, TaskStatus status) {
        return createSupervisedTaskStack(taskId, title, description, status, null);
    }

    public ItemStack createSupervisedTaskStack(
            Integer taskId,
            String title,
            String description,
            TaskStatus status,
            String assigneeIgn
    ) {
        return createTaskStack(taskId, title, description, status, null, assigneeIgn, null);
    }

    public ItemStack createIdleTaskStack(
            Integer taskId,
            String title,
            String description,
            Date dateAssigned,
            String assigneeName,
            String advisorName
    ) {
        return createTaskStack(taskId, title, description, TaskStatus.DOING, dateAssigned, assigneeName, advisorName);
    }

    private ItemStack createTaskStack(
            Integer taskId,
            String title,
            String description,
            TaskStatus status,
            Date dateAssigned,
            String assigneeName,
            String advisorName
    ) {
        Material material = Material.ORANGE_CONCRETE;
        switch (status) {
            case FINISHED -> material = Material.LIME_CONCRETE;
            case APPROVED -> material = Material.LIGHT_BLUE_CONCRETE;
            case PREPARED -> material = Material.LIGHT_GRAY_CONCRETE;
        }
        ItemStack stack = new ItemStack(material);

        ItemMeta meta = new ItemMetaBuilder()
                .setDisplayName(ChatColor.BLUE + "" + ChatColor.BOLD + title + " " + ChatColor.DARK_GRAY + "#" + taskId)
                .setPersistentData(new NamespacedKey(plugin, DataContainerKeys.TASK_ID), PersistentDataType.INTEGER, taskId)
                .setLore(createTaskStackLore(assigneeName, advisorName, dateAssigned, description))
                .build();

        stack.setItemMeta(meta);
        return stack;
    }

    private List<String> createTaskStackLore(String assigneeName, String advisorName, Date dateAssigned, String description) {
        List<String> lore = new ArrayList<>();

        if (assigneeName != null) {
            lore.add(ChatColor.GRAY + "Assignee: " + ChatColor.GOLD + assigneeName);
        }
        if (advisorName != null) {
            lore.add(ChatColor.GRAY + "Advisor: " + ChatColor.GOLD + advisorName);
        }
        if (dateAssigned != null) {
            LocalDate currentDate = LocalDate.now();
            LocalDate sqlLocalDate = dateAssigned.toLocalDate();
            long daysDelta = ChronoUnit.DAYS.between(sqlLocalDate, currentDate);
            lore.add(0, ChatColor.GRAY + "Duration: " + ChatColor.GOLD + daysDelta + " days");
        }

        addTaskDescriptionToLore(lore, description);
        return lore;
    }

    private void addTaskDescriptionToLore(List<String> lore, String input) {
        String[] words = input.split("\\s+");
        StringBuilder currentString = new StringBuilder();

        for (String word : words) {
            if (currentString.length() + word.length() + 1 <= LORE_WIDTH_LIMIT) {
                if (!currentString.isEmpty()) {
                    currentString.append(" ");
                }
                currentString.append(word);
            } else {
                lore.add(ChatColor.GRAY + currentString.toString());
                currentString = new StringBuilder(word);
            }
        }

        if (!currentString.isEmpty()) {
            lore.add(ChatColor.GRAY + currentString.toString());
        }
    }

    /**
     * Creates ItemStack of Material.WRITABLE_BOOK for creating new task
     *
     * @param ign         name of member for which to create task, for given name created task will be directly assigned
     *                    to the member (in {@link me.colormaestro.taskmanager.enums.TaskStatus#DOING} state), otherwise null
     *                    value causes that task will be only prepared (in {@link me.colormaestro.taskmanager.enums.TaskStatus#PREPARED} state)
     * @param description instructions what to do in the task
     * @return ItemStack for creating the task
     */
    public ItemStack createAssignmentBook(String ign, String description) {
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
            bookMeta.getPersistentDataContainer()
                    .set(new NamespacedKey(plugin, DataContainerKeys.BOOK_ACTION), PersistentDataType.STRING, Directives.CREATE_TASK);
            bookMeta.getPersistentDataContainer()
                    .set(new NamespacedKey(plugin, DataContainerKeys.MEMBER_NAME), PersistentDataType.STRING, ign);
        } else {
            bookMeta.setDisplayName(ChatColor.GRAY + "Book for creation of prepared task");
            bookMeta.getPersistentDataContainer()
                    .set(new NamespacedKey(plugin, DataContainerKeys.BOOK_ACTION), PersistentDataType.STRING, Directives.PREPARE_TASK);

        }

        book.setItemMeta(bookMeta);
        return book;
    }
}
