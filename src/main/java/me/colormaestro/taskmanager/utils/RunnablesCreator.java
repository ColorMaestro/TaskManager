package me.colormaestro.taskmanager.utils;

import me.colormaestro.taskmanager.data.DataAccessException;
import me.colormaestro.taskmanager.data.MemberDAO;
import me.colormaestro.taskmanager.data.TaskDAO;
import me.colormaestro.taskmanager.model.AdvisedTask;
import me.colormaestro.taskmanager.model.IdleTask;
import me.colormaestro.taskmanager.model.Member;
import me.colormaestro.taskmanager.model.MemberDashboardInfo;
import me.colormaestro.taskmanager.model.Task;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.sql.SQLException;
import java.util.List;

public class RunnablesCreator {
    private static final int PAGE_SIZE = 45;
    private static final int LAST_ROW_MIDDLE = 49;
    private static final int LAST_ROW_LEFT_FROM_MIDDLE = 48;
    private static final int LAST_ROW_RIGHT_FROM_MIDDLE = 50;

    private final TaskDAO taskDAO;
    private final MemberDAO memberDAO;
    private final Plugin plugin;

    private final ItemStackCreator stackCreator;

    public RunnablesCreator(Plugin plugin, TaskDAO taskDAO, MemberDAO memberDAO) {
        this.plugin = plugin;
        this.taskDAO = taskDAO;
        this.memberDAO = memberDAO;
        this.stackCreator = new ItemStackCreator(plugin);
    }

    public Runnable showDashboardView(HumanEntity player, long page) {
        return () -> {
            try {
                List<MemberDashboardInfo> stats = taskDAO.fetchMembersDashboardInfo();
                int totalPages = stats.size() / PAGE_SIZE + 1;
                // Variable used in lambda should be final or effectively final
                List<MemberDashboardInfo> finalStats = getPageFromList(stats, page);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    String inventoryTitle = ChatColor.BLUE + "" + ChatColor.BOLD + "Tasks Dashboard" + ChatColor.RESET + " (" + page + "/" + totalPages + ") " + Directives.DASHBOARD;
                    InventoryBuilder builder = new InventoryBuilder(player, inventoryTitle);

                    ItemStack stack;
                    int position = 0;
                    for (MemberDashboardInfo memberInfo : finalStats) {
                        stack = stackCreator.createMemberStack(
                                memberInfo.uuid(),
                                memberInfo.ign(),
                                memberInfo.doing(),
                                memberInfo.finished(),
                                memberInfo.approved(),
                                memberInfo.lastLogin());
                        builder.addItemStack(position, stack);
                        position++;
                    }

                    builder.addPaginationArrows()
                            .addItemStack(LAST_ROW_MIDDLE, Material.ENDER_EYE,
                                    ChatColor.DARK_PURPLE + "Show supervised tasks")
                            .addItemStack(LAST_ROW_LEFT_FROM_MIDDLE, Material.LIGHT_GRAY_CONCRETE,
                                    ChatColor.GRAY + "Show prepared tasks")
                            .addItemStack(LAST_ROW_RIGHT_FROM_MIDDLE, Material.CLOCK,
                                    ChatColor.GOLD + "Show idle tasks");

                    player.openInventory(builder.build());
                });
            } catch (SQLException ex) {
                Bukkit.getScheduler().runTask(plugin,
                        () -> player.sendMessage(ChatColor.RED + ex.getMessage()));
                ex.printStackTrace();
            }
        };
    }

    public Runnable showActiveTasksView(HumanEntity player, String ign, long page) {
        return () -> {
            try {
                Member member = memberDAO.findMember(ign);
                List<Task> tasks = taskDAO.fetchPlayersActiveTasks(member.getId());
                int totalPages = tasks.size() / PAGE_SIZE + 1;
                List<Task> finalTasks = getPageFromList(tasks, page);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    String title = ChatColor.BLUE + "" + ChatColor.BOLD + ign + "'s tasks" + ChatColor.RESET + " (" + page + "/" + totalPages + ") " + Directives.ACTIVE_TASKS;
                    InventoryBuilder builder = new InventoryBuilder(player, title);

                    ItemStack stack;
                    int position = 0;
                    for (Task task : finalTasks) {
                        stack = stackCreator.createBasicTaskStack(
                                task.getId(),
                                task.getTitle(),
                                task.getDescription(),
                                task.getStatus());
                        builder.addItemStack(position, stack);
                        position++;
                    }

                    var approvedTasksLink = new ItemStack(Material.LIGHT_BLUE_CONCRETE);
                    ItemMeta meta = approvedTasksLink.getItemMeta();
                    assert meta != null;
                    meta.setDisplayName(ChatColor.AQUA + "Show " + ign + "'s approved tasks");
                    meta.getPersistentDataContainer().set(
                            new NamespacedKey(plugin, DataContainerKeys.MEMBER_NAME), PersistentDataType.STRING, ign);
                    approvedTasksLink.setItemMeta(meta);

                    builder.addPaginationArrows()
                            .addItemStack(LAST_ROW_MIDDLE, approvedTasksLink)
                            .addItemStack(LAST_ROW_LEFT_FROM_MIDDLE, Material.SPECTRAL_ARROW,
                                    ChatColor.AQUA + "Back to dashboard");

                    player.openInventory(builder.build());
                });
            } catch (SQLException | DataAccessException ex) {
                Bukkit.getScheduler().runTask(plugin,
                        () -> player.sendMessage(ChatColor.RED + ex.getMessage()));
                ex.printStackTrace();
            }
        };
    }

    public Runnable showApprovedTasksView(HumanEntity player, String ign, long page) {
        return () -> {
            try {
                Member member = memberDAO.findMember(ign);
                List<Task> tasks = taskDAO.fetchPlayersApprovedTasks(member.getId());
                int totalPages = tasks.size() / PAGE_SIZE + 1;
                List<Task> finalTasks = getPageFromList(tasks, page);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    String title = ChatColor.DARK_AQUA + "" + ChatColor.BOLD + ign + "'s approved tasks" + ChatColor.RESET + " (" + page + "/" + totalPages + ") " + Directives.APPROVED_TASKS;
                    InventoryBuilder builder = new InventoryBuilder(player, title);

                    ItemStack stack;
                    int position = 0;
                    for (Task task : finalTasks) {
                        stack = stackCreator.createBasicTaskStack(
                                task.getId(),
                                task.getTitle(),
                                task.getDescription(),
                                task.getStatus());
                        builder.addItemStack(position, stack);
                        position++;
                    }

                    var activeTasksLink = new ItemStack(Material.SPECTRAL_ARROW);
                    ItemMeta meta = activeTasksLink.getItemMeta();
                    assert meta != null;
                    meta.setDisplayName(ChatColor.AQUA + "Back to active tasks");
                    meta.getPersistentDataContainer().set(
                            new NamespacedKey(plugin, DataContainerKeys.MEMBER_NAME), PersistentDataType.STRING, ign);
                    activeTasksLink.setItemMeta(meta);

                    builder.addPaginationArrows().addItemStack(LAST_ROW_MIDDLE, activeTasksLink);

                    player.openInventory(builder.build());
                });
            } catch (SQLException | DataAccessException ex) {
                Bukkit.getScheduler().runTask(plugin,
                        () -> player.sendMessage(ChatColor.RED + ex.getMessage()));
                ex.printStackTrace();
            }
        };
    }

    public Runnable showSupervisedTasksView(HumanEntity player, long page) {
        return () -> {
            try {
                Member member = memberDAO.findMember(player.getName());
                List<AdvisedTask> tasks = taskDAO.fetchAdvisorActiveTasks(member.getId());
                int totalPages = tasks.size() / PAGE_SIZE + 1;
                List<AdvisedTask> finalTasks = getPageFromList(tasks, page);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    String title = ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Your supervised tasks" + ChatColor.RESET + " (" + page + "/" + totalPages + ") " + Directives.SUPERVISED_TASKS;
                    InventoryBuilder builder = new InventoryBuilder(player, title);

                    ItemStack stack;
                    int position = 0;
                    for (AdvisedTask task : finalTasks) {
                        stack = stackCreator.createSupervisedTaskStack(
                                task.id(),
                                task.title(),
                                task.description(),
                                task.status(),
                                task.ign());
                        builder.addItemStack(position, stack);
                        position++;
                    }

                    builder.addPaginationArrows()
                            .addItemStack(LAST_ROW_MIDDLE, Material.SPECTRAL_ARROW,
                                    ChatColor.AQUA + "Back to dashboard");

                    player.openInventory(builder.build());
                });
            } catch (SQLException | DataAccessException ex) {
                Bukkit.getScheduler().runTask(plugin,
                        () -> player.sendMessage(ChatColor.RED + ex.getMessage()));
                ex.printStackTrace();
            }
        };
    }

    public Runnable showPreparedTasksView(HumanEntity player, long page) {
        return () -> {
            try {
                List<Task> tasks = taskDAO.fetchPreparedTasks();
                int totalPages = tasks.size() / PAGE_SIZE + 1;
                List<Task> finalTasks = getPageFromList(tasks, page);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    String title = ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "Prepared tasks" + ChatColor.RESET + " (" + page + "/" + totalPages + ") " + Directives.PREPARED_TASKS;
                    InventoryBuilder builder = new InventoryBuilder(player, title);

                    ItemStack stack;
                    int position = 0;
                    for (Task task : finalTasks) {
                        stack = stackCreator.createBasicTaskStack(
                                task.getId(),
                                task.getTitle(),
                                task.getDescription(),
                                task.getStatus());
                        builder.addItemStack(position, stack);
                        position++;
                    }

                    builder.addPaginationArrows()
                            .addItemStack(LAST_ROW_MIDDLE, Material.SPECTRAL_ARROW,
                                    ChatColor.AQUA + "Back to dashboard");

                    player.openInventory(builder.build());
                });
            } catch (SQLException ex) {
                Bukkit.getScheduler().runTask(plugin,
                        () -> player.sendMessage(ChatColor.RED + ex.getMessage()));
                ex.printStackTrace();
            }
        };
    }

    public Runnable showIdleTasksView(HumanEntity player, long page) {
        return () -> {
            try {
                List<IdleTask> tasks = taskDAO.fetchIdleTasks();
                int totalPages = tasks.size() / PAGE_SIZE + 1;
                List<IdleTask> finalTasks = getPageFromList(tasks, page);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    String title = ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Idle tasks" + ChatColor.RESET + " (" + page + "/" + totalPages + ") " + Directives.IDLE_TASKS;
                    InventoryBuilder builder = new InventoryBuilder(player, title);

                    ItemStack stack;
                    int position = 0;
                    for (IdleTask task : finalTasks) {
                        stack = stackCreator.createIdleTaskStack(
                                task.id(),
                                task.title(),
                                task.description(),
                                task.dateAssigned(),
                                task.assigneeName(),
                                task.advisorName());
                        builder.addItemStack(position, stack);
                        position++;
                    }

                    builder.addPaginationArrows()
                            .addItemStack(LAST_ROW_MIDDLE, Material.SPECTRAL_ARROW,
                                    ChatColor.AQUA + "Back to dashboard");

                    player.openInventory(builder.build());
                });
            } catch (SQLException ex) {
                Bukkit.getScheduler().runTask(plugin,
                        () -> player.sendMessage(ChatColor.RED + ex.getMessage()));
                ex.printStackTrace();
            }
        };
    }

    public Runnable teleportPlayerToTask(HumanEntity player, int taskId) {
        return () -> {
            try {
                Task task = taskDAO.findTask(taskId);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    double x = task.getX();
                    double y = task.getY();
                    double z = task.getZ();
                    float yaw = task.getYaw();
                    float pitch = task.getPitch();

                    player.closeInventory();
                    player.teleport(new Location(player.getWorld(), x, y, z, yaw, pitch));
                });
            } catch (SQLException | DataAccessException ex) {
                Bukkit.getScheduler().runTask(plugin,
                        () -> player.sendMessage(ChatColor.RED + ex.getMessage()));
                ex.printStackTrace();
            }
        };
    }

    public Runnable givePlayerAssignmentBook(Player player, int taskId) {
        return () -> {
            try {
                Task task = taskDAO.findTask(taskId);

                int creatorID = task.getCreatorID();
                Integer advisorID = task.getAdvisorID();
                Integer assigneeID = task.getAssigneeID();

                String creatorName = memberDAO.findMember(creatorID).getIgn();
                String advisorName = advisorID != null ? memberDAO.findMember(advisorID).getIgn() : "Unassigned";
                String assigneeName = assigneeID != null ? memberDAO.findMember(assigneeID).getIgn() : "Unassigned";
                Bukkit.getScheduler().runTask(plugin, () -> {
                    ItemStack book = createTaskBook(task, creatorName, advisorName, assigneeName);
                    player.getInventory().addItem(book);
                });
            } catch (SQLException | DataAccessException ex) {
                Bukkit.getScheduler().runTask(plugin,
                        () -> player.sendMessage(ChatColor.RED + ex.getMessage()));
                ex.printStackTrace();
            }
        };
    }

    public Plugin getPlugin() {
        return plugin;
    }

    private ItemStack createTaskBook(Task task, String creatorName, String advisorName, String assigneeName) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta) book.getItemMeta();

        net.md_5.bungee.api.ChatColor assigneeNameColor = task.getAssigneeID() != null ?
                net.md_5.bungee.api.ChatColor.GOLD : net.md_5.bungee.api.ChatColor.GRAY;

        net.md_5.bungee.api.ChatColor advisorNameColor = task.getAdvisorID() != null ?
                net.md_5.bungee.api.ChatColor.GOLD : net.md_5.bungee.api.ChatColor.GRAY;

        ComponentBuilder builder = new ComponentBuilder("   " + task.getTitle() + "\n")
                .color(net.md_5.bungee.api.ChatColor.BLUE).bold(true)
                .append("Creator: ")
                .color(net.md_5.bungee.api.ChatColor.RESET).bold(false)
                .append(creatorName + "\n")
                .color(net.md_5.bungee.api.ChatColor.GOLD)
                .append("Advisor: ")
                .color(net.md_5.bungee.api.ChatColor.RESET).bold(false)
                .append(advisorName + "\n")
                .color(advisorNameColor)
                .append("Assignee: ")
                .color(net.md_5.bungee.api.ChatColor.RESET).bold(false)
                .append(assigneeName + "\n")
                .color(assigneeNameColor)
                .append("Created: " + task.getDateCreation() + "\n")
                .color(net.md_5.bungee.api.ChatColor.RESET);

        if (task.getDateAssigned() != null) {
            builder = builder.append("Assigned: " + task.getDateAssigned() + "\n");
        }

        if (task.getDateCompleted() != null) {
            builder = builder.append("Finished: " + task.getDateCompleted() + "\n");
        }
        builder = builder.append("\nTurn the page for instructions!");

        BaseComponent[] page = builder.create();

        BaseComponent[] page2 = new ComponentBuilder(task.getDescription()).create();

        bookMeta.spigot().addPage(page);
        bookMeta.spigot().addPage(page2);
        bookMeta.setTitle("blank");
        ChatColor tittleColor = switch (task.getStatus()) {
            case DOING -> ChatColor.GOLD;
            case FINISHED -> ChatColor.GREEN;
            case APPROVED -> ChatColor.AQUA;
            case PREPARED -> ChatColor.GRAY;
        };
        bookMeta.setDisplayName(tittleColor + "" + ChatColor.BOLD + "Task " + task.getId());
        bookMeta.setAuthor(advisorName);
        book.setItemMeta(bookMeta);
        return book;
    }

    private <T> List<T> getPageFromList(List<T> list, long page) {
        return list.stream().skip((page - 1) * PAGE_SIZE).limit(PAGE_SIZE).toList();
    }
}
