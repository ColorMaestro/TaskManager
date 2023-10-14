package me.colormaestro.taskmanager.model;

import java.sql.Date;

public record IdleTask(Integer id, String title, String description,
                       Date dateAssigned, String assigneeName, String advisorName) {
}
