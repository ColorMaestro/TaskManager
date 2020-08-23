package me.colormaestro.taskmanager;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;

public final class TaskManager extends JavaPlugin {
    private File configFile;
    private FileConfiguration config;

    @Override
    public void onEnable() {
        loadConfig();
        this.getCommand("tasks").setExecutor(new Tasks(config));
        this.getCommand("addtask").setExecutor(new AddTask(config));
        this.getCommand("finishtask").setExecutor(new FinishTask(config));
        this.getCommand("approvetask").setExecutor(new ApproveTask(config));
        this.getCommand("returntask").setExecutor(new ReturnTask(config));
        this.getCommand("visittask").setExecutor(new VisitTask(config));
        this.getCommand("settaskplace").setExecutor(new SetTaskPlace(config));

        this.getCommand("tasks").setTabCompleter(new TasksTabCompleter(config));
        this.getCommand("addtask").setTabCompleter(new TasksTabCompleter(config));
    }

    @Override
    public void onDisable() {

    }

    private void loadConfig() {
        configFile = new File(getDataFolder(), "config.yml");
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
}
