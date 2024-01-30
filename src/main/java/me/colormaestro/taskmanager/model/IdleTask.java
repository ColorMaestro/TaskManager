package me.colormaestro.taskmanager.model;

import org.bukkit.ChatColor;

import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record IdleTask(Integer id, String title, String description,
                       Date dateAssigned, String assigneeName, String advisorName) implements StringReporter {
    @Override
    public String getReport() {
        LocalDate currentDate = LocalDate.now();
        LocalDate sqlLocalDate = dateAssigned.toLocalDate();
        long daysDelta = ChronoUnit.DAYS.between(sqlLocalDate, currentDate);
        return ChatColor.GOLD + "[" + id + "] " + ChatColor.WHITE + title + ChatColor.ITALIC + " (" + daysDelta + " days)";
    }
}
