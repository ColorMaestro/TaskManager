package me.colormaestro.taskmanager.model;

import java.sql.Date;

public class Member {
    private Integer id;
    private final String uuid;
    private final String ign;
    private final Date lastOnline;
    private final Long discordID;
    private final boolean active;

    public Member(String uuid, String ign, Date lastOnline, Long discordID, boolean active) {
        this.uuid = uuid;
        this.ign = ign;
        this.lastOnline = lastOnline;
        this.discordID = discordID;
        this.active = active;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public String getUuid() {
        return uuid;
    }

    public String getIgn() {
        return ign;
    }

    public Date getLastOnline() {
        return lastOnline;
    }

    public Long getDiscordID() {
        return discordID;
    }

    public boolean isActive() {
        return active;
    }
}
