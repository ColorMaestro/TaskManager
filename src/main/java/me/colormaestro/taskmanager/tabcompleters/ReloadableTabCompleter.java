package me.colormaestro.taskmanager.tabcompleters;

import org.bukkit.command.TabCompleter;

public interface ReloadableTabCompleter extends TabCompleter {

    /**
     * Reloads suggested tab completions for command from external source.
     */
    void reload();
}
