package me.colormaestro.taskmanager;

import me.colormaestro.taskmanager.data.PlayerDAO;
import me.colormaestro.taskmanager.data.TaskDAO;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.Objects;

public final class TaskManager extends JavaPlugin {
    private FileConfiguration config;
    private TaskDAO taskDAO;
    private PlayerDAO playerDAO;

    @Override
    public void onEnable() {
        loadConfig();
        createDAOs();
        Objects.requireNonNull(this.getCommand("tasks")).setExecutor(new Tasks(config));
        Objects.requireNonNull(this.getCommand("addtask")).setExecutor(new AddTask(taskDAO, playerDAO));
        Objects.requireNonNull(this.getCommand("finishtask")).setExecutor(new FinishTask(taskDAO, playerDAO));
        Objects.requireNonNull(this.getCommand("approvetask")).setExecutor(new ApproveTask(taskDAO));
        Objects.requireNonNull(this.getCommand("visittask")).setExecutor(new VisitTask(taskDAO));
        Objects.requireNonNull(this.getCommand("settaskplace")).setExecutor(new SetTaskPlace(config));

        Objects.requireNonNull(this.getCommand("tasks")).setTabCompleter(new TasksTabCompleter(config));
        Objects.requireNonNull(this.getCommand("addtask")).setTabCompleter(new TasksTabCompleter(config));
    }

    @Override
    public void onDisable() {

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
        taskDAO = new TaskDAO(getDataFolder().getAbsolutePath());
        playerDAO = new PlayerDAO(getDataFolder().getAbsolutePath());
    }
}
