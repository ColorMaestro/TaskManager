package me.colormaestro.taskmanager.commands;

import me.colormaestro.taskmanager.data.MemberDAO;
import me.colormaestro.taskmanager.data.TaskDAO;
import me.colormaestro.taskmanager.integrations.DecentHologramsIntegration;
import me.colormaestro.taskmanager.integrations.DynmapIntegration;
import me.colormaestro.taskmanager.model.Task;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;

public class SynchronizeTasks implements CommandExecutor {
    private final BukkitScheduler scheduler = Bukkit.getScheduler();
    private final Plugin plugin;
    private final TaskDAO taskDAO;
    private final MemberDAO memberDAO;
    private final DecentHologramsIntegration decentHolograms;
    private final DynmapIntegration dynmap;

    public SynchronizeTasks(Plugin plugin, TaskDAO taskDAO, MemberDAO memberDAO,
                            DecentHologramsIntegration decentHolograms, DynmapIntegration dynmap) {
        this.plugin = plugin;
        this.taskDAO = taskDAO;
        this.memberDAO = memberDAO;
        this.decentHolograms = decentHolograms;
        this.dynmap = dynmap;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        scheduler.runTaskAsynchronously(plugin, () -> {
            try {
                List<Task> activeTasks = taskDAO.fetchActiveTasks();

                scheduler.runTask(plugin, () -> dynmap.overwriteActiveTasks(activeTasks));
            } catch (SQLException ex) {
                scheduler.runTask(plugin, () -> sender.sendMessage(ChatColor.RED + ex.getMessage()));
                ex.printStackTrace();
            }
        });
        return true;
    }
}
