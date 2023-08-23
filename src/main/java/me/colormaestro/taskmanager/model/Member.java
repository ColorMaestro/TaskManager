package me.colormaestro.taskmanager.model;

public class Member {
    private Integer id;
    private final String uuid;
    private final String ign;
    private final long discordID;

    public Member(String uuid, String ign, long discordID) {
        this.uuid = uuid;
        this.ign = ign;
        this.discordID = discordID;
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

    public long getDiscordID() {
        return discordID;
    }
}
