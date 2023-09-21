package me.colormaestro.taskmanager.model;

import me.colormaestro.taskmanager.enums.TaskStatus;

import java.sql.Date;

public class Task {
    private Integer id;
    private final String title;
    private final String description;
    private final Integer assigneeID;
    private final Integer advisorID;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;
    private final TaskStatus status;
    private final Date dateCreation;
    private final Date dateCompleted;

    public Task(String title, String description, Integer assigneeID, Integer advisorID,
                double x, double y, double z, float yaw, float pitch,
                TaskStatus status, Date dateCreation, Date dateCompleted) {
        this.title = title;
        this.description = description;
        this.assigneeID = assigneeID;
        this.advisorID = advisorID;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.status = status;
        this.dateCreation = dateCreation;
        this.dateCompleted = dateCompleted;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Integer getAssigneeID() {
        return assigneeID;
    }

    public Integer getAdvisorID() {
        return advisorID;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public Date getDateCreation() {
        return dateCreation;
    }

    public Date getDateCompleted() {
        return dateCompleted;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }
}
