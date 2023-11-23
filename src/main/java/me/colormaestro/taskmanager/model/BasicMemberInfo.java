package me.colormaestro.taskmanager.model;

import java.sql.Date;

public record BasicMemberInfo(String ign, String uuid, int doing, int finished, int approved, Date lastLogin) {

}
