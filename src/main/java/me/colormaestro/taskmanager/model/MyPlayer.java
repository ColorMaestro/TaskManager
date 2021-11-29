package me.colormaestro.taskmanager.model;

/**
 * Represents player from database point of view, not Bukkit.
 */
public class MyPlayer {
    private Integer id;
    private final String uuid;
    private final String ign;
    private final long discordID;

    public MyPlayer(String uuid, String ign, long discordID) {
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
