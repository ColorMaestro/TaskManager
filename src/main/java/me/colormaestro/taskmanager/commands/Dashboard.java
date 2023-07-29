package me.colormaestro.taskmanager.commands;

import me.colormaestro.taskmanager.data.TaskDAO;
import me.colormaestro.taskmanager.model.MemberTaskStats;
import me.colormaestro.taskmanager.utils.Directives;
import me.colormaestro.taskmanager.utils.ItemStackBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Dashboard implements CommandExecutor {
    private final Plugin plugin;
    private final TaskDAO taskDAO;
    private static final int INVENTORY_SIZE = 54;
    private static final int PREVIOUS_PAGE_POSITION = 45;
    private static final int NEXT_PAGE_POSITION = 53;

    public Dashboard(Plugin plugin, TaskDAO taskDAO) {
        this.plugin = plugin;
        this.taskDAO = taskDAO;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can't be run from console.");
            return true;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                List<MemberTaskStats> stats = taskDAO.fetchTaskStatistics();
                int totalPages = stats.size() / (INVENTORY_SIZE - 9) + 1;
                // Variable used in lambda should be final or effectively final
                List<MemberTaskStats> finalStats = stats.stream().limit(INVENTORY_SIZE - 9).toList();
                Bukkit.getScheduler().runTask(plugin,
                        () -> {
                            String inventoryTitle = ChatColor.BLUE + "" + ChatColor.BOLD + "Tasks Dashboard" + ChatColor.RESET + " (1/" + totalPages + ") " + Directives.DASHBOARD;
                            Inventory inventory = Bukkit.createInventory(player, INVENTORY_SIZE, inventoryTitle);

                            ItemStack stack;
                            int position = 0;
                            for (MemberTaskStats memberStats : finalStats) {
                                stack = ItemStackBuilder.buildMemberStack(memberStats.uuid(), memberStats.ign(), memberStats.doing(), memberStats.finished(), memberStats.approved());
                                inventory.setItem(position, stack);
                                position++;
                            }

                            stack = new ItemStack(Material.ARROW, 1);
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

                            player.openInventory(inventory);
                        });
            } catch (SQLException ex) {
                Bukkit.getScheduler().runTask(plugin,
                        () -> player.sendMessage(ChatColor.RED + ex.getMessage()));
                ex.printStackTrace();
            }

        });

        return true;
    }
}
