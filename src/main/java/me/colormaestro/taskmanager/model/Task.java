package me.colormaestro.taskmanager.model;

import me.colormaestro.taskmanager.enums.TaskStatus;
import org.bukkit.ChatColor;

import java.sql.Date;

public class Task implements StringReporter {
    private Integer id;
    private final String title;
    private final String description;
    private final int creatorID;
    private final Integer assigneeID;
    private final Integer advisorID;
    private final String worldName;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;
    private final TaskStatus status;
    private final Date dateCreation;
    private final Date dateAssigned;
    private final Date dateCompleted;

    public Task(String title, String description, int creatorID, Integer assigneeID, Integer advisorID,
                String worldName, double x, double y, double z, float yaw, float pitch,
                TaskStatus status, Date dateCreation, Date dateAssigned, Date dateCompleted) {
        this.title = title;
        this.description = description;
        this.creatorID = creatorID;
        this.assigneeID = assigneeID;
        this.advisorID = advisorID;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.status = status;
        this.dateCreation = dateCreation;
        this.dateAssigned = dateAssigned;
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

    public int getCreatorID() {
        return creatorID;
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

    public Date getDateAssigned() {
        return dateAssigned;
    }

    public Date getDateCompleted() {
        return dateCompleted;
    }

    public String getWorldName() {
        return worldName;
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

    @Override
    public String getReport() {
        return status.color + "[" + id + "] " + ChatColor.WHITE + title;
    }
}
