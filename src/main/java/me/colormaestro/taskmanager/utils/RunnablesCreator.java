package me.colormaestro.taskmanager.utils;

import me.colormaestro.taskmanager.data.DataAccessException;
import me.colormaestro.taskmanager.data.MemberDAO;
import me.colormaestro.taskmanager.data.TaskDAO;
import me.colormaestro.taskmanager.integrations.DecentHologramsIntegration;
import me.colormaestro.taskmanager.integrations.DiscordOperator;
import me.colormaestro.taskmanager.model.AdvisedTask;
import me.colormaestro.taskmanager.model.IdleTask;
import me.colormaestro.taskmanager.model.Member;
import me.colormaestro.taskmanager.model.BasicMemberInfo;
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
import java.util.UUID;

public class RunnablesCreator {
    private static final int PAGE_SIZE = 45;
    private static final int LAST_ROW_MIDDLE = 49;
    private static final int LAST_ROW_LEFT_FROM_MIDDLE = 48;
    private static final int LAST_ROW_RIGHT_FROM_MIDDLE = 50;
    private static final int LAST_ROW_THIRD = 47;

    private final TaskDAO taskDAO;
    private final MemberDAO memberDAO;
    private final Plugin plugin;
    private final DecentHologramsIntegration decentHolograms;
    private final ItemStackCreator stackCreator;

    public RunnablesCreator(Plugin plugin, TaskDAO taskDAO, MemberDAO memberDAO,
                            DecentHologramsIntegration decentHolograms) {
        this.plugin = plugin;
        this.taskDAO = taskDAO;
        this.memberDAO = memberDAO;
        this.decentHolograms = decentHolograms;
        this.stackCreator = new ItemStackCreator(plugin);
    }

    public Runnable showDashboardView(HumanEntity player, int page) {
        return () -> {
            try {
                List<BasicMemberInfo> stats = taskDAO.fetchMembersDashboardInfo();
                int totalPages = stats.size() / PAGE_SIZE + 1;
                // Variable used in lambda should be final or effectively final
                List<BasicMemberInfo> finalStats = getPageFromList(stats, page);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    String inventoryTitle = ChatColor.BLUE + "" + ChatColor.BOLD + "Tasks Dashboard" + ChatColor.RESET + " (" + page + "/" + totalPages + ") " + Directives.DASHBOARD;
                    InventoryBuilder builder = new InventoryBuilder(player, inventoryTitle);

                    ItemStack stack;
                    int position = 0;
                    for (BasicMemberInfo memberInfo : finalStats) {
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

                    ItemStack previousPageLink = new ItemStack(Material.ARROW);
                    previousPageLink.setItemMeta(createPaginationItemMeta(page, totalPages, false));
                    ItemStack nextPageLink = new ItemStack(Material.ARROW);
                    nextPageLink.setItemMeta(createPaginationItemMeta(page, totalPages, true));

                    builder.addPaginationItemStacks(previousPageLink, nextPageLink)
                            .addItemStack(LAST_ROW_MIDDLE, Material.ENDER_EYE,
                                    ChatColor.DARK_PURPLE + "Show supervised tasks")
                            .addItemStack(LAST_ROW_LEFT_FROM_MIDDLE, Material.LIGHT_GRAY_CONCRETE,
                                    ChatColor.GRAY + "Show prepared tasks")
                            .addItemStack(LAST_ROW_RIGHT_FROM_MIDDLE, Material.CLOCK,
                                    ChatColor.GOLD + "Show idle tasks")
                            .addItemStack(LAST_ROW_THIRD, Material.PAPER,
                                    ChatColor.GOLD + "Show members who need tasks");

                    player.openInventory(builder.build());
                });
            } catch (SQLException ex) {
                Bukkit.getScheduler().runTask(plugin,
                        () -> player.sendMessage(ChatColor.RED + ex.getMessage()));
                ex.printStackTrace();
            }
        };
    }

    public Runnable showActiveTasksView(HumanEntity player, String ign, int page) {
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
                    ItemMeta meta = new ItemMetaBuilder()
                            .setDisplayName(ChatColor.AQUA + "Show " + ign + "'s approved tasks")
                            .setPersistentData(
                                    new NamespacedKey(plugin, DataContainerKeys.MEMBER_NAME),
                                    PersistentDataType.STRING,
                                    ign)
                            .build();
                    approvedTasksLink.setItemMeta(meta);

                    ItemStack previousPageLink = new ItemStack(Material.ARROW);
                    previousPageLink.setItemMeta(createPaginationItemMeta(page, totalPages, false, ign));
                    ItemStack nextPageLink = new ItemStack(Material.ARROW);
                    nextPageLink.setItemMeta(createPaginationItemMeta(page, totalPages, true, ign));

                    builder.addPaginationItemStacks(previousPageLink, nextPageLink)
                            .addItemStack(LAST_ROW_MIDDLE, approvedTasksLink)
                            .addItemStack(LAST_ROW_RIGHT_FROM_MIDDLE, Material.WRITABLE_BOOK,
                                    ChatColor.GOLD + "Add new task for " + ign)
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

    public Runnable showApprovedTasksView(HumanEntity player, String ign, int page) {
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
                    ItemMeta meta = new ItemMetaBuilder()
                            .setDisplayName(ChatColor.AQUA + "Back to active tasks")
                            .setPersistentData(
                                    new NamespacedKey(plugin, DataContainerKeys.MEMBER_NAME),
                                    PersistentDataType.STRING,
                                    ign)
                            .build();
                    activeTasksLink.setItemMeta(meta);

                    ItemStack previousPageLink = new ItemStack(Material.ARROW);
                    previousPageLink.setItemMeta(createPaginationItemMeta(page, totalPages, false, ign));
                    ItemStack nextPageLink = new ItemStack(Material.ARROW);
                    nextPageLink.setItemMeta(createPaginationItemMeta(page, totalPages, true, ign));

                    builder.addPaginationItemStacks(previousPageLink, nextPageLink)
                            .addItemStack(LAST_ROW_MIDDLE, activeTasksLink);

                    player.openInventory(builder.build());
                });
            } catch (SQLException | DataAccessException ex) {
                Bukkit.getScheduler().runTask(plugin,
                        () -> player.sendMessage(ChatColor.RED + ex.getMessage()));
                ex.printStackTrace();
            }
        };
    }

    public Runnable showSupervisedTasksView(HumanEntity player, int page) {
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

                    ItemStack previousPageLink = new ItemStack(Material.ARROW);
                    previousPageLink.setItemMeta(createPaginationItemMeta(page, totalPages, false));
                    ItemStack nextPageLink = new ItemStack(Material.ARROW);
                    nextPageLink.setItemMeta(createPaginationItemMeta(page, totalPages, true));

                    builder.addPaginationItemStacks(previousPageLink, nextPageLink)
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

    public Runnable showPreparedTasksView(HumanEntity player, int page) {
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
                                task.getStatus(),
                                List.of(ChatColor.YELLOW + "➜ Left-Click to teleport",
                                        ChatColor.AQUA + "➜ Right-Click to assign")
                        );
                        builder.addItemStack(position, stack);
                        position++;
                    }

                    ItemStack previousPageLink = new ItemStack(Material.ARROW);
                    previousPageLink.setItemMeta(createPaginationItemMeta(page, totalPages, false));
                    ItemStack nextPageLink = new ItemStack(Material.ARROW);
                    nextPageLink.setItemMeta(createPaginationItemMeta(page, totalPages, true));

                    builder.addPaginationItemStacks(previousPageLink, nextPageLink)
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

    public Runnable showIdleTasksView(HumanEntity player, int page) {
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

                    ItemStack previousPageLink = new ItemStack(Material.ARROW);
                    previousPageLink.setItemMeta(createPaginationItemMeta(page, totalPages, false));
                    ItemStack nextPageLink = new ItemStack(Material.ARROW);
                    nextPageLink.setItemMeta(createPaginationItemMeta(page, totalPages, true));

                    builder.addPaginationItemStacks(previousPageLink, nextPageLink)
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
    
    public Runnable showNeedTasksView(HumanEntity player, int limit, int page) {
        return () -> {
            try {
                List<BasicMemberInfo> stats = taskDAO
                        .fetchMembersDashboardInfo()
                        .stream()
                        .filter(basicMemberInfo -> basicMemberInfo.doing() <= limit)
                        .toList();
                int totalPages = stats.size() / PAGE_SIZE + 1;
                List<BasicMemberInfo> finalStats = getPageFromList(stats, page);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    String title = ChatColor.RED + "" + ChatColor.BOLD + "Members with " + limit + " or less tasks" + ChatColor.RESET + " (" + page + "/" + totalPages + ") " + Directives.NEED_TASKS;
                    InventoryBuilder builder = new InventoryBuilder(player, title);

                    ItemStack stack;
                    int position = 0;
                    for (BasicMemberInfo memberInfo : finalStats) {
                        stack = stackCreator.createNeedTasksStack(
                                memberInfo.uuid(),
                                memberInfo.ign(),
                                memberInfo.doing());
                        builder.addItemStack(position, stack);
                        position++;
                    }

                    ItemStack previousPageLink = new ItemStack(Material.ARROW);
                    previousPageLink.setItemMeta(createPaginationItemMeta(page, totalPages, false));
                    ItemStack nextPageLink = new ItemStack(Material.ARROW);
                    nextPageLink.setItemMeta(createPaginationItemMeta(page, totalPages, true));

                    ItemStack decreaseLimit = new ItemStack(Material.SMALL_AMETHYST_BUD);
                    decreaseLimit.setItemMeta(createLimitMeta(limit, false));
                    ItemStack increaseLimit = new ItemStack(Material.AMETHYST_CLUSTER);
                    increaseLimit.setItemMeta(createLimitMeta(limit, true));

                    builder.addPaginationItemStacks(previousPageLink, nextPageLink)
                            .addItemStack(LAST_ROW_LEFT_FROM_MIDDLE, decreaseLimit)
                            .addItemStack(LAST_ROW_RIGHT_FROM_MIDDLE, increaseLimit)
                            .addItemStack(LAST_ROW_MIDDLE, Material.SPECTRAL_ARROW,
                                    ChatColor.AQUA + "Back to dashboard");

                    player.getPersistentDataContainer().set(
                            new NamespacedKey(plugin, DataContainerKeys.CURRENT_LIMIT),
                            PersistentDataType.INTEGER,
                            limit);

                    player.openInventory(builder.build());
                });
            } catch (SQLException ex) {
                Bukkit.getScheduler().runTask(plugin,
                        () -> player.sendMessage(ChatColor.RED + ex.getMessage()));
                ex.printStackTrace();
            }
        };
    }

    public Runnable showAssignTasksView(HumanEntity player, int taskId, int page) {
        return () -> {
            try {
                List<BasicMemberInfo> stats = taskDAO.fetchMembersDashboardInfo();
                int totalPages = stats.size() / PAGE_SIZE + 1;
                // Variable used in lambda should be final or effectively final
                List<BasicMemberInfo> finalStats = getPageFromList(stats, page);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    String inventoryTitle = ChatColor.BLUE + "" + ChatColor.BOLD + "Select Member" + ChatColor.RESET + " (" + page + "/" + totalPages + ") " + Directives.SELECT_MEMBER;
                    InventoryBuilder builder = new InventoryBuilder(player, inventoryTitle);

                    ItemStack stack;
                    int position = 0;
                    for (BasicMemberInfo memberInfo : finalStats) {
                        stack = new ItemStack(Material.PLAYER_HEAD);

                        ItemMeta meta = new SkullMetaBuilder()
                                .setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(memberInfo.uuid())))
                                .setDisplayName(ChatColor.BLUE + "" + ChatColor.BOLD + memberInfo.ign())
                                .setLore(List.of(ChatColor.YELLOW + "➜ Click to assign"))
                                .setPersistentData(
                                        new NamespacedKey(plugin, DataContainerKeys.MEMBER_NAME),
                                        PersistentDataType.STRING,
                                        memberInfo.ign())
                                .build();

                        stack.setItemMeta(meta);
                        builder.addItemStack(position, stack);
                        position++;
                    }

                    player.getPersistentDataContainer().set(
                            new NamespacedKey(plugin, DataContainerKeys.TASK_ID),
                            PersistentDataType.INTEGER,
                            taskId);

                    ItemStack previousPageLink = new ItemStack(Material.ARROW);
                    previousPageLink.setItemMeta(createPaginationItemMeta(page, totalPages, false));
                    ItemStack nextPageLink = new ItemStack(Material.ARROW);
                    nextPageLink.setItemMeta(createPaginationItemMeta(page, totalPages, true));

                    builder.addPaginationItemStacks(previousPageLink, nextPageLink)
                            .addItemStack(LAST_ROW_MIDDLE, Material.SPECTRAL_ARROW,
                                    ChatColor.GRAY + "Back to prepared tasks");

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

    public Runnable assignTask(String ign, HumanEntity player, int taskId) {
        return () -> {
            UUID uuid = player.getUniqueId();
            Member assignee, advisor;
            try {
                assignee = memberDAO.findMember(ign);
                advisor = memberDAO.findMember(uuid);
            } catch (SQLException ex) {
                Bukkit.getScheduler().runTask(plugin, () -> player.sendMessage(ChatColor.RED + ex.getMessage()));
                ex.printStackTrace();
                return;
            } catch (DataAccessException ignored) {
                Bukkit.getScheduler().runTask(plugin, () -> player.sendMessage(ChatColor.GOLD + "Player " + ign +
                        " is not registered as member. Use" + ChatColor.DARK_AQUA + " /addmember " + ign +
                        ChatColor.GOLD + " for adding player as member, then you can add tasks."));
                return;
            }

            try {
                taskDAO.assignTask(taskId, assignee.getId(), advisor.getId());
                Task task = taskDAO.findTask(taskId);
                List<Task> activeTasks = taskDAO.fetchPlayersActiveTasks(assignee.getId());
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.sendMessage(ChatColor.GREEN + "Task assigned.");
                    decentHolograms.setTasks(assignee.getUuid(), activeTasks);

                    boolean messageSent = MessageSender.sendMessageIfOnline(
                            assignee.getUuid(),
                            ChatColor.GOLD + "You have new task from " + player.getName()
                    );

                    if (!messageSent && assignee.getDiscordID() != null) {
                        DiscordOperator.getInstance().taskCreated(assignee.getDiscordID(), player.getName(), task);
                    }
                });
            } catch (SQLException | DataAccessException ex) {
                Bukkit.getScheduler().runTask(plugin,
                        () -> player.sendMessage(ChatColor.RED + ex.getMessage()));
                ex.printStackTrace();
            } catch (NumberFormatException ex) {
                Bukkit.getScheduler().runTask(plugin,
                        () -> player.sendMessage(ChatColor.RED + "Task ID must be numerical value!"));
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
        bookMeta.setDisplayName(task.getStatus().color + "" + ChatColor.BOLD + "Task " + task.getId());
        bookMeta.setAuthor(advisorName);
        book.setItemMeta(bookMeta);
        return book;
    }

    private <T> List<T> getPageFromList(List<T> list, int page) {
        return list.stream().skip((long) (page - 1) * PAGE_SIZE).limit(PAGE_SIZE).toList();
    }

    private ItemMeta createPaginationItemMeta(int currentPage, int totalPages, boolean turnNextPage) {
        return createPaginationItemMeta(currentPage, totalPages, turnNextPage, null);
    }

    private ItemMeta createPaginationItemMeta(int currentPage, int totalPages, boolean turnNextPage, String memberName) {
        ItemMetaBuilder builder = new ItemMetaBuilder()
                .setDisplayName(turnNextPage ? "Next page" : "Previous page")
                .setPersistentData(
                        new NamespacedKey(plugin, DataContainerKeys.CURRENT_PAGE),
                        PersistentDataType.INTEGER,
                        currentPage)
                .setPersistentData(
                        new NamespacedKey(plugin, DataContainerKeys.TOTAL_PAGES),
                        PersistentDataType.INTEGER,
                        totalPages);

        if (turnNextPage) {
            builder.setPersistentData(
                    new NamespacedKey(plugin, DataContainerKeys.TURN_NEXT_PAGE),
                    PersistentDataType.STRING,
                    "yes");
        }

        if (memberName != null) {
            builder.setPersistentData(
                    new NamespacedKey(plugin, DataContainerKeys.MEMBER_NAME),
                    PersistentDataType.STRING,
                    memberName);
        }

        return builder.build();
    }

    private ItemMeta createLimitMeta(int currentLimit, boolean increaseLimit) {
        ItemMetaBuilder builder = new ItemMetaBuilder()
                .setDisplayName(increaseLimit ? "Increase limit" : "Decrease limit")
                .setPersistentData(
                        new NamespacedKey(plugin, DataContainerKeys.CURRENT_LIMIT),
                        PersistentDataType.INTEGER,
                        currentLimit);

        return builder.build();
    }
}
