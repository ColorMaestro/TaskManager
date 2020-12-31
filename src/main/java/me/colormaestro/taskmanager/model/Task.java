package me.colormaestro.taskmanager.model;

import me.colormaestro.taskmanager.enums.TaskStatus;

import java.sql.Date;

public class Task {
    private Integer id;
    private String description;
    private int assigneeID;
    private int advisorID;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private TaskStatus status;
    private Date dateCreation;
    private Date dateCompleted;

    public Task(String description, int assigneeID, int advisorID,
                double x, double y, double z, float yaw, float pitch,
                TaskStatus status, Date dateCreation, Date dateCompleted) {
        this.description = description;
        this.assigneeID = assigneeID;
        this.advisorID = advisorID;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.status = TaskStatus.DOING;
        this.dateCreation = new Date(System.currentTimeMillis());
    }

    public Integer getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public int getAssigneeID() {
        return assigneeID;
    }

    public int getAdvisorID() {
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
