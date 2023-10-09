package me.colormaestro.taskmanager.model;

import java.sql.Date;

/**
 * Represents single record in result of statistic tasks query.
 */
public record MemberDashboardInfo(String ign, String uuid, int doing, int finished, int approved, Date lastLogin) {

}
