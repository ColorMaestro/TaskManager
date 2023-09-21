package me.colormaestro.taskmanager;

import me.colormaestro.taskmanager.commands.AddMember;
import me.colormaestro.taskmanager.commands.AddTask;
import me.colormaestro.taskmanager.commands.ApproveTask;
import me.colormaestro.taskmanager.commands.Dashboard;
import me.colormaestro.taskmanager.commands.Establish;
import me.colormaestro.taskmanager.commands.FinishTask;
import me.colormaestro.taskmanager.commands.LinkDiscord;
import me.colormaestro.taskmanager.commands.PrepareTask;
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
import me.colormaestro.taskmanager.listeners.ActiveTasksViewListener;
import me.colormaestro.taskmanager.listeners.ApprovedTasksViewListener;
import me.colormaestro.taskmanager.listeners.BookEditListener;
import me.colormaestro.taskmanager.listeners.DashboardViewListener;
import me.colormaestro.taskmanager.listeners.PlayerJoinListener;
import me.colormaestro.taskmanager.listeners.PreparedTasksViewListener;
import me.colormaestro.taskmanager.listeners.SupervisedTasksViewListener;
import me.colormaestro.taskmanager.tabcompleters.MembersTabCompleter;
import me.colormaestro.taskmanager.tabcompleters.ReloadableTabCompleter;
import me.colormaestro.taskmanager.tabcompleters.TasksTabCompleter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
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
        ReloadableTabCompleter tasksTabCompleter = new TasksTabCompleter(playerDAO);
        ReloadableTabCompleter membersTabCompleter = new MembersTabCompleter(playerDAO);
        Objects.requireNonNull(this.getCommand("tasks")).setTabCompleter(tasksTabCompleter);
        Objects.requireNonNull(this.getCommand("addtask")).setTabCompleter(membersTabCompleter);
        Objects.requireNonNull(this.getCommand("dashboard")).setTabCompleter(membersTabCompleter);

        registerEventListener(new PlayerJoinListener(this, taskDAO, playerDAO));
        registerEventListener(new BookEditListener(this, taskDAO, playerDAO));
        registerEventListener(new DashboardViewListener(this, taskDAO, playerDAO));
        registerEventListener(new SupervisedTasksViewListener(this, taskDAO, playerDAO));
        registerEventListener(new ActiveTasksViewListener(this, taskDAO, playerDAO));
        registerEventListener(new ApprovedTasksViewListener(this, taskDAO, playerDAO));
        registerEventListener(new PreparedTasksViewListener(this, taskDAO, playerDAO));

        setCommandExecutor("addmember", new AddMember(this, playerDAO, tasksTabCompleter, membersTabCompleter));
        setCommandExecutor("dashboard", new Dashboard(this, taskDAO, playerDAO));
        setCommandExecutor("tasks", new Tasks(this, taskDAO, playerDAO));
        setCommandExecutor("addtask", new AddTask(this, taskDAO));
        setCommandExecutor("preparetask", new PrepareTask());
        setCommandExecutor("finishtask", new FinishTask(taskDAO, playerDAO));
        setCommandExecutor("approvetask", new ApproveTask(taskDAO, playerDAO));
        setCommandExecutor("visittask", new VisitTask(taskDAO, playerDAO));
        setCommandExecutor("returntask", new ReturnTask(taskDAO, playerDAO));
        setCommandExecutor("settaskplace", new SetTaskPlace(taskDAO, playerDAO));
        setCommandExecutor("linkdiscord", new LinkDiscord());
        setCommandExecutor("establish", new Establish(taskDAO, playerDAO));
        setCommandExecutor("taskinfo", new TaskInfo(taskDAO, playerDAO));
        setCommandExecutor("transfertask", new TransferTask(taskDAO, playerDAO));

        if (Bukkit.getPluginManager().isPluginEnabled("DecentHolograms")) {
            HologramLayer.instantiate();
            this.getLogger().info("DecentHolograms plugin detected, TaskManager will be fully functional");
        } else {
            this.getLogger().info("DecentHolograms plugin was not detected, functionality will be limited");
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

    private void registerEventListener(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }

    private void setCommandExecutor(String command, CommandExecutor executor) {
        Objects.requireNonNull(this.getCommand(command)).setExecutor(executor);
    }
}
