package me.colormaestro.taskmanager.model;

import me.colormaestro.taskmanager.enums.TaskStatus;
import org.bukkit.ChatColor;

public record AdvisedTask(Integer id, String title, String description, TaskStatus status, String ign) implements StringReporter {

    @Override
    public String getReport() {
        return status.color + "[" + id + "] " + ChatColor.WHITE + title + ChatColor.ITALIC + " (" + ign + ")";
    }
}
