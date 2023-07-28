package me.colormaestro.taskmanager.model;

/**
 * Represents single record in result of statistic tasks query.
 */
public record MemberTaskStats(String ign, String uuid, int doing, int finished, int approved) {

}
