package me.colormaestro.taskmanager;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class TasksTabCompleter implements TabCompleter {
    private final FileConfiguration config;

    public TasksTabCompleter(FileConfiguration config) {
        this.config = config;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (args.length > 0) {
                MemorySection memorySection = (MemorySection) config.get("translation_table");
                Set<String> set = memorySection.getKeys(false);
                List<String> list = new LinkedList<>();
                for (String s : set) {
                    if (s.toLowerCase().contains(args[0].toLowerCase())) {
                        list.add(s);
                    }
                }
                if ("help".contains(args[0].toLowerCase())) {
                    list.add("help");
                }
                return list;
            }
        }
        return null;
    }
}
