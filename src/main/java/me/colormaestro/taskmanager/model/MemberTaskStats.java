package me.colormaestro.taskmanager.model;

/**
 * Represents single record in result of statistic tasks query.
 */
public record MemberTaskStats(String ign, int doing, int finished, int approved) {

}
