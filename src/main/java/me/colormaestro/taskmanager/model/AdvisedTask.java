package me.colormaestro.taskmanager.model;

import me.colormaestro.taskmanager.enums.TaskStatus;

public record AdvisedTask(Integer id, String title, String description, TaskStatus status, String ign) {

}
