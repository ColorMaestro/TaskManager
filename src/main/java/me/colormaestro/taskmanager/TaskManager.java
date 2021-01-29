package me.colormaestro.taskmanager;

import com.sainttx.holograms.api.HologramPlugin;
import me.colormaestro.taskmanager.commands.AddTask;
import me.colormaestro.taskmanager.commands.ApproveTask;
import me.colormaestro.taskmanager.commands.Establish;
import me.colormaestro.taskmanager.commands.FinishTask;
import me.colormaestro.taskmanager.commands.SetTaskPlace;
import me.colormaestro.taskmanager.commands.Tasks;
import me.colormaestro.taskmanager.commands.VisitTask;
import me.colormaestro.taskmanager.data.HologramLayer;
import me.colormaestro.taskmanager.data.PlayerDAO;
import me.colormaestro.taskmanager.data.TaskDAO;
import me.colormaestro.taskmanager.listeners.CustomListener;
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
        getServer().getPluginManager().registerEvents(new CustomListener(taskDAO, playerDAO), this);
        Objects.requireNonNull(this.getCommand("tasks")).setExecutor(new Tasks(taskDAO, playerDAO));
        Objects.requireNonNull(this.getCommand("addtask")).setExecutor(new AddTask());
        Objects.requireNonNull(this.getCommand("finishtask")).setExecutor(new FinishTask(taskDAO, playerDAO));
        Objects.requireNonNull(this.getCommand("approvetask")).setExecutor(new ApproveTask(taskDAO, playerDAO));
        Objects.requireNonNull(this.getCommand("visittask")).setExecutor(new VisitTask(taskDAO, playerDAO));
        Objects.requireNonNull(this.getCommand("settaskplace")).setExecutor(new SetTaskPlace(taskDAO, playerDAO));
        Objects.requireNonNull(this.getCommand("establish")).setExecutor(new Establish());

        Objects.requireNonNull(this.getCommand("tasks")).setTabCompleter(new TasksTabCompleter(playerDAO));
        Objects.requireNonNull(this.getCommand("addtask")).setTabCompleter(new TasksTabCompleter(playerDAO));
        HologramLayer.instantiate(JavaPlugin.getPlugin(HologramPlugin.class).getHologramManager());
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
