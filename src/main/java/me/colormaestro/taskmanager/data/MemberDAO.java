package me.colormaestro.taskmanager.data;

import me.colormaestro.taskmanager.model.Member;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MemberDAO {
    private final String url;

    public MemberDAO(String dataFolderPath) {
        Path path = Paths.get(dataFolderPath, "db.sqlite");
        this.url = "jdbc:sqlite:" + path;
        initTable();
    }

    private void initTable() {
        if (!tableExists()) {
            createTable();
        }
        if (!viewExists()) {
            createView();
        }
    }

    private boolean tableExists() {
        try (Connection connection = DriverManager.getConnection(url);
             ResultSet rs = connection.getMetaData().getTables(null, null, "MEMBERS", null)) {
            return rs.next();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to detect if the table MEMBERS exist", ex);
        }
    }

    private boolean viewExists() {
        try (Connection connection = DriverManager.getConnection(url);
             ResultSet rs = connection.getMetaData().getTables(null, null, "ACTIVE_MEMBERS", new String[]{"VIEW"})) {
            return rs.next();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to detect if the view ACTIVE_MEMBERS exist", ex);
        }
    }

    private void createTable() {
        try (Connection connection = DriverManager.getConnection(url);
             Statement st = connection.createStatement()) {

            st.executeUpdate("CREATE TABLE MEMBERS (" +
                    "id INTEGER PRIMARY KEY," +
                    "uuid VARCHAR(36) NOT NULL," +
                    "ign VARCHAR(30) NOT NULL," +
                    "last_login DATE NOT NULL," +
                    "discord_id INTEGER," +
                    "active BOOL DEFAULT true" +
                    ")");
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to create MEMBERS table", ex);
        }
    }

    private void createView() {
        try (Connection connection = DriverManager.getConnection(url);
             Statement st = connection.createStatement()) {

            st.executeUpdate("CREATE VIEW ACTIVE_MEMBERS AS SELECT * FROM MEMBERS WHERE active = true");
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to create ACTIVE_MEMBERS view", ex);
        }
    }

    /**
     * @param id of the member
     * @return Member record
     * @throws SQLException        if SQL error arise
     * @throws DataAccessException if member is not found
     */
    public synchronized Member findMember(int id) throws SQLException, DataAccessException {
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement st = connection.prepareStatement(
                     "SELECT id, uuid, ign, last_login, discord_id, active FROM MEMBERS WHERE id = ?")) {

            st.setInt(1, id);
            ResultSet rs = st.executeQuery();
            if (rs.isClosed()) {
                throw new DataAccessException("Failed to get member with id " + id + ".");
            }
            Member member = new Member(
                    rs.getString("uuid"),
                    rs.getString("ign"),
                    rs.getDate("last_login"),
                    ParsingUtils.getLongOrNull(rs, "discord_id"),
                    rs.getBoolean("active"));
            member.setId(rs.getInt("id"));
            rs.close();
            return member;
        }
    }

    /**
     * @param ign of member
     * @return Member record
     * @throws SQLException        if SQL error arise
     * @throws DataAccessException if member is not found
     */
    public synchronized Member findMember(String ign) throws SQLException, DataAccessException {
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement st = connection.prepareStatement(
                     "SELECT id, uuid, ign, last_login, discord_id, active FROM MEMBERS WHERE ign = ?")) {

            st.setString(1, ign);
            ResultSet rs = st.executeQuery();
            if (rs.isClosed()) {
                throw new DataAccessException("Failed to get member with name " + ign + ".");
            }
            Member member = new Member(
                    rs.getString("uuid"),
                    rs.getString("ign"),
                    rs.getDate("last_login"),
                    ParsingUtils.getLongOrNull(rs, "discord_id"),
                    rs.getBoolean("active"));
            member.setId(rs.getInt("id"));
            rs.close();
            return member;
        }
    }

    /**
     * @param uuid of member
     * @return Member record
     * @throws SQLException        if SQL error arise
     * @throws DataAccessException if member is not found
     */
    public synchronized Member findMember(UUID uuid) throws SQLException, DataAccessException {
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement st = connection.prepareStatement(
                     "SELECT id, uuid, ign, last_login, discord_id, active FROM MEMBERS WHERE uuid = ?")) {

            st.setString(1, uuid.toString());
            ResultSet rs = st.executeQuery();
            if (rs.isClosed()) {
                throw new DataAccessException("Failed to get member with uuid " + uuid + ".");
            }
            Member member = new Member(
                    rs.getString("uuid"),
                    rs.getString("ign"),
                    rs.getDate("last_login"),
                    ParsingUtils.getLongOrNull(rs, "discord_id"),
                    rs.getBoolean("active"));
            member.setId(rs.getInt("id"));
            rs.close();
            return member;
        }
    }

    /**
     * Gets all member in game names
     *
     * @return list of all members' in game names
     * @throws SQLException if SQL error arise
     */
    public synchronized List<String> getMembersNames() throws SQLException {
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement st = connection.prepareStatement(
                     "SELECT ign FROM ACTIVE_MEMBERS")) {
            ResultSet rs = st.executeQuery();
            List<String> result = new ArrayList<>();
            while (rs.next()) {
                result.add(rs.getString("ign"));
            }
            rs.close();
            return result;
        }
    }

    /**
     * Checks whether there is record about member in DB.
     *
     * @param uuid according to which to seek
     * @return true if there's matching record, false otherwise
     * @throws SQLException if SQL error arise
     */
    public synchronized boolean memberExists(String uuid) throws SQLException {
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement st = connection.prepareStatement(
                     "SELECT ign FROM MEMBERS WHERE uuid = ?")) {
            st.setString(1, uuid);
            ResultSet rs = st.executeQuery();
            if (rs.isClosed()) {
                return false;
            }
            rs.close();
            return true;
        }
    }

    /**
     * Creates record about member.
     *
     * @param uuid member's uuid
     * @param ign  member's ign
     * @throws SQLException if SQL error arise
     */
    public synchronized void addMember(String uuid, String ign) throws SQLException {
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement st = connection.prepareStatement(
                     "INSERT INTO MEMBERS (uuid, ign, last_login) VALUES (?, ?, ?)")) {
            st.setString(1, uuid);
            st.setString(2, ign);
            st.setDate(3, new Date(System.currentTimeMillis()));
            st.executeUpdate();
        }
    }

    /**
     * Sets member's discord ID.
     *
     * @param uuid      member's uuid
     * @param discordID ID of member's discord account
     * @throws SQLException        if SQL error arise
     * @throws DataAccessException if there's no matching record for member
     */
    public synchronized void setDiscordUserID(UUID uuid, long discordID) throws SQLException, DataAccessException {
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement st = connection.prepareStatement(
                     "UPDATE MEMBERS SET discord_id = ? WHERE uuid = ?")) {
            st.setLong(1, discordID);
            st.setString(2, uuid.toString());
            int affected = st.executeUpdate();
            if (affected == 0) {
                throw new DataAccessException("No discord ID change for member with uuid " + uuid + ".");
            }
        }
    }

    /**
     * Updates member's name
     *
     * @param uuid member's uuid
     * @param name to save
     * @throws SQLException        if SQL error arise
     * @throws DataAccessException if there's no matching record for member
     */
    public synchronized void updateMemberName(UUID uuid, String name) throws SQLException, DataAccessException {
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement st = connection.prepareStatement(
                     "UPDATE MEMBERS SET ign = ? WHERE uuid = ?")) {
            st.setString(1, name);
            st.setString(2, uuid.toString());
            int affected = st.executeUpdate();
            if (affected == 0) {
                throw new DataAccessException("Failed to update member name with uuid " + uuid + ".");
            }
        }
    }

    /**
     * Updates member's last online time to current time.
     *
     * @param uuid member's uuid
     * @throws SQLException if SQL error arise
     */
    public synchronized void updateLastLoginTime(UUID uuid) throws SQLException {
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement st = connection.prepareStatement(
                     "UPDATE MEMBERS SET last_login = ? WHERE uuid = ?")) {
            st.setDate(1, new Date(System.currentTimeMillis()));
            st.setString(2, uuid.toString());
            st.executeUpdate();
        }
    }

    /**
     * Sets member's activity.
     *
     * @param ign member's name
     * @param active whether member should be marked as active or not
     * @throws SQLException if SQL error arise
     */
    public synchronized void updateActivity(String ign, boolean active) throws SQLException, DataAccessException {
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement st = connection.prepareStatement(
                     "UPDATE MEMBERS SET active = ? WHERE ign = ?")) {
            st.setBoolean(1, active);
            st.setString(2, ign);
            int affected = st.executeUpdate();
            if (affected == 0) {
                throw new DataAccessException("Failed to update activity for member with name " + ign + ".");
            }
        }
    }
}
