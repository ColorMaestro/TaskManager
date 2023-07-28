package me.colormaestro.taskmanager.commands;

import me.colormaestro.taskmanager.data.TaskDAO;
import me.colormaestro.taskmanager.model.MemberTaskStats;
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

    public Dashboard(Plugin plugin, TaskDAO taskDAO) {
        this.plugin = plugin;
        this.taskDAO = taskDAO;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can't be run from console.");
            return true;
        }

        Player player = (Player) sender;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                List<MemberTaskStats> stats = taskDAO.fetchTaskStatistics().stream().limit(INVENTORY_SIZE - 9).toList();

                Bukkit.getScheduler().runTask(plugin,
                        () -> {
                            String inventoryTitle = ChatColor.BLUE + "" + ChatColor.BOLD + "Tasks Dashboard" + ChatColor.RESET + " (#1)";
                            Inventory inventory = Bukkit.createInventory(player, INVENTORY_SIZE, inventoryTitle);

                            ItemStack stack;
                            int position = 0;
                            for (MemberTaskStats memberStats : stats) {
                                stack = getHeadForUUID(memberStats.uuid(), memberStats.ign(), memberStats.doing(), memberStats.finished(), memberStats.approved());
                                inventory.setItem(position, stack);
                                position++;
                            }

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

    public ItemStack getHeadForUUID(String uuid, String ign, int doing, int finished, int approved) {
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

    public List<String> buildHeadStatsLore(int doing, int finished, int approved) {
        List<String> result = new ArrayList<>();

        result.add(ChatColor.GRAY + "Opened: " + ChatColor.GOLD + doing);
        result.add(ChatColor.GRAY + "Finished: " + ChatColor.GREEN + finished);
        result.add(ChatColor.GRAY + "Approved: " + ChatColor.AQUA + approved);

        return result;
    }
}
