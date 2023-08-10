package me.colormaestro.taskmanager;

import me.colormaestro.taskmanager.commands.AddMember;
import me.colormaestro.taskmanager.commands.AddTask;
import me.colormaestro.taskmanager.commands.ApproveTask;
import me.colormaestro.taskmanager.commands.Dashboard;
import me.colormaestro.taskmanager.commands.Establish;
import me.colormaestro.taskmanager.commands.FinishTask;
import me.colormaestro.taskmanager.commands.LinkDiscord;
import me.colormaestro.taskmanager.commands.ReturnTask;
import me.colormaestro.taskmanager.commands.SetTaskPlace;
import me.colormaestro.taskmanager.commands.TaskInfo;
import me.colormaestro.taskmanager.commands.Tasks;
import me.colormaestro.taskmanager.commands.TransferTask;
import me.colormaestro.taskmanager.commands.VisitTask;
import me.colormaestro.taskmanager.data.DiscordManager;
import me.colormaestro.taskmanager.data.HologramLayer;
import me.colormaestro.taskmanager.data.PlayerDAO;
import me.colormaestro.taskmanager.data.TaskDAO;
import me.colormaestro.taskmanager.listeners.ApprovedTasksViewListener;
import me.colormaestro.taskmanager.listeners.BookEditListener;
import me.colormaestro.taskmanager.listeners.PlayerJoinListener;
import me.colormaestro.taskmanager.listeners.DashboardViewListener;
import me.colormaestro.taskmanager.listeners.ActiveTasksViewListener;
import me.colormaestro.taskmanager.listeners.SupervisedTasksViewListener;
import me.colormaestro.taskmanager.tabcompleters.AddTaskTabCompleter;
import me.colormaestro.taskmanager.tabcompleters.TasksTabCompleter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public final class TaskManager extends JavaPlugin {
    private FileConfiguration config;
    private TaskDAO taskDAO;
    private PlayerDAO playerDAO;

    @Override
    public void onEnable() {
        loadConfig();
        createDAOs();
        TasksTabCompleter tasksTabCompleter = new TasksTabCompleter(playerDAO);
        AddTaskTabCompleter addTaskTabCompleter = new AddTaskTabCompleter(playerDAO);
        Objects.requireNonNull(this.getCommand("tasks")).setTabCompleter(tasksTabCompleter);
        Objects.requireNonNull(this.getCommand("addtask")).setTabCompleter(addTaskTabCompleter);

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this, taskDAO, playerDAO), this);
        getServer().getPluginManager().registerEvents(new BookEditListener(this, taskDAO, playerDAO), this);
        getServer().getPluginManager().registerEvents(new DashboardViewListener(this, taskDAO, playerDAO), this);
        getServer().getPluginManager().registerEvents(new SupervisedTasksViewListener(this, taskDAO, playerDAO), this);
        getServer().getPluginManager().registerEvents(new ActiveTasksViewListener(this, taskDAO, playerDAO), this);
        getServer().getPluginManager().registerEvents(new ApprovedTasksViewListener(this, taskDAO, playerDAO), this);

        Objects.requireNonNull(this.getCommand("addmember")).setExecutor(new AddMember(this, playerDAO, tasksTabCompleter, addTaskTabCompleter));
        Objects.requireNonNull(this.getCommand("dashboard")).setExecutor(new Dashboard(this, taskDAO));
        Objects.requireNonNull(this.getCommand("tasks")).setExecutor(new Tasks(this, taskDAO, playerDAO));
        Objects.requireNonNull(this.getCommand("addtask")).setExecutor(new AddTask(this, taskDAO));
        Objects.requireNonNull(this.getCommand("finishtask")).setExecutor(new FinishTask(taskDAO, playerDAO));
        Objects.requireNonNull(this.getCommand("approvetask")).setExecutor(new ApproveTask(taskDAO, playerDAO));
        Objects.requireNonNull(this.getCommand("visittask")).setExecutor(new VisitTask(taskDAO, playerDAO));
        Objects.requireNonNull(this.getCommand("returntask")).setExecutor(new ReturnTask(taskDAO, playerDAO));
        Objects.requireNonNull(this.getCommand("settaskplace")).setExecutor(new SetTaskPlace(taskDAO, playerDAO));
        Objects.requireNonNull(this.getCommand("linkdiscord")).setExecutor(new LinkDiscord());
        Objects.requireNonNull(this.getCommand("establish")).setExecutor(new Establish());
        Objects.requireNonNull(this.getCommand("taskinfo")).setExecutor(new TaskInfo(taskDAO, playerDAO));
        Objects.requireNonNull(this.getCommand("transfertask")).setExecutor(new TransferTask(taskDAO, playerDAO));

        if (Bukkit.getPluginManager().isPluginEnabled("Holograms")) {
            HologramLayer.instantiate();
            this.getLogger().info("Holograms plugin detected, TaskManager will be fully functional");
        } else {
            this.getLogger().info("Holograms plugin was not detected, functionality will be limited");
        }
        DiscordManager.instantiate(config.getString("token"), playerDAO, this);
    }

    @Override
    public void onDisable() {
        DiscordManager.getInstance().shutdown();
    }

    private void loadConfig() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            saveResource("config.yml", false);
        }

        config = new YamlConfiguration();
        try {
            config.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    private void createDAOs() {
        playerDAO = new PlayerDAO(getDataFolder().getAbsolutePath());
        taskDAO = new TaskDAO(getDataFolder().getAbsolutePath());
    }
}
