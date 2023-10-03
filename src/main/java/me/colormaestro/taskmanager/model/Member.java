package me.colormaestro.taskmanager.model;

public class Member {
    private Integer id;
    private final String uuid;
    private final String ign;
    private final Long discordID;

    public Member(String uuid, String ign, Long discordID) {
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

    public Long getDiscordID() {
        return discordID;
    }
}
