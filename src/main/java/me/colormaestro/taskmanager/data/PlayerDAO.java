package me.colormaestro.taskmanager.data;

import me.colormaestro.taskmanager.model.MyPlayer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerDAO {
    private final String url;

    public PlayerDAO(String dataFolderPath) {
        Path path = Paths.get(dataFolderPath, "db.sqlite");
        this.url = "jdbc:sqlite:" + path;
        initTable();
    }

    private void initTable() {
        if (!tableExits()) {
            createTable();
        }
    }

    private boolean tableExits() {
        try (Connection connection = DriverManager.getConnection(url);
             ResultSet rs = connection.getMetaData().getTables(null, null, "PLAYERS", null)) {
            return rs.next();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to detect if the table PLAYERS exist", ex);
        }
    }

    private void createTable() {
        try (Connection connection = DriverManager.getConnection(url);
             Statement st = connection.createStatement()) {

            st.executeUpdate("CREATE TABLE PLAYERS (" +
                    "id INTEGER PRIMARY KEY," +
                    "uuid VARCHAR(36) NOT NULL," +
                    "ign VARCHAR(30) NOT NULL," +
                    "discord_id INTEGER" +
                    ")");
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to create PLAYERS table", ex);
        }
    }

    public synchronized Map<Integer, MyPlayer> fetchAllPlayers() throws SQLException {
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement st = connection.prepareStatement(
                     "SELECT id, uuid, ign, discord_id FROM PLAYERS"
             )) {
            ResultSet rs = st.executeQuery();
            Map<Integer, MyPlayer> players = new HashMap<>();
            while (rs.next()) {
                MyPlayer player = new MyPlayer(
                        rs.getString("uuid"),
                        rs.getString("ign"),
                        rs.getLong("discord_id")
                );
                player.setId(rs.getInt("id"));
                players.put(player.getId(), player);
            }
            rs.close();
            return players;
        }
    }

    public synchronized int getPlayerID(String ign) throws SQLException, DataAccessException {
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement st = connection.prepareStatement(
                     "SELECT id FROM PLAYERS WHERE ign = ?"
             )) {
            st.setString(1, ign);
            ResultSet rs = st.executeQuery();
            if (rs.isClosed()) {
                throw new DataAccessException("Player with name " + ign + " was not found in the database.");
            }
            int id = rs.getInt("id");
            rs.close();
            return id;
        }
    }

    public synchronized int getPlayerID(UUID uuid) throws SQLException, DataAccessException {
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement st = connection.prepareStatement(
                     "SELECT id FROM PLAYERS WHERE uuid = ?"
             )) {
            st.setString(1, uuid.toString());
            ResultSet rs = st.executeQuery();
            if (rs.isClosed()) {
                throw new DataAccessException("Your uuid was not found in the database. Contact developers!");
            }
            int id = rs.getInt("id");
            rs.close();
            return id;
        }
    }

    public synchronized String getPlayerIGN(int id) throws SQLException, DataAccessException {
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement st = connection.prepareStatement(
                     "SELECT ign FROM PLAYERS WHERE id = ?"
             )) {
            st.setInt(1, id);
            ResultSet rs = st.executeQuery();
            if (rs.isClosed()) {
                throw new DataAccessException("Failed to get player IGN according to UUID. Contact developers!");
            }
            String ign = rs.getString("ign");
            rs.close();
            return ign;
        }
    }

    public synchronized String getPlayerUUID(int id) throws SQLException, DataAccessException {
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement st = connection.prepareStatement(
                     "SELECT uuid FROM PLAYERS WHERE id = ?"
             )) {
            st.setInt(1, id);
            ResultSet rs = st.executeQuery();
            if (rs.isClosed()) {
                throw new DataAccessException("Failed to get player UUID according to UUID. Contact developers!");
            }
            String ign = rs.getString("uuid");
            rs.close();
            return ign;
        }
    }

    public synchronized List<String> getAllIGN() throws SQLException {
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement st = connection.prepareStatement(
                     "SELECT ign FROM PLAYERS")) {
            ResultSet rs = st.executeQuery();
            List<String> result = new ArrayList<>();
            while (rs.next()) {
                result.add(rs.getString("ign"));
            }
            rs.close();
            return result;
        }
    }

    public synchronized boolean playerExists(String uuid) throws SQLException {
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement st = connection.prepareStatement(
                     "SELECT ign FROM PLAYERS WHERE uuid = ?")) {
            st.setString(1, uuid);
            ResultSet rs = st.executeQuery();
            if (rs.isClosed()) {
                return false;
            }
            rs.close();
            return true;
        }
    }

    public synchronized void addPlayer(String uuid, String ign) throws SQLException {
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement st = connection.prepareStatement(
                     "INSERT INTO PLAYERS (uuid, ign) VALUES (?, ?)")) {
            st.setString(1, uuid);
            st.setString(2, ign);
            st.executeUpdate();
        }
    }

    public synchronized long getDiscordUserID(String uuid) throws SQLException, DataAccessException {
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement st = connection.prepareStatement(
                     "SELECT discord_id FROM PLAYERS WHERE uuid = ?"
             )) {
            st.setString(1, uuid);
            ResultSet rs = st.executeQuery();
            if (rs.isClosed()) {
                throw new DataAccessException("Player with uuid " + uuid + " was not found in the database.");
            }
            long id = rs.getLong("discord_id");
            rs.close();
            return id;
        }
    }

    public synchronized void setDiscordUserID(UUID uuid, long discordID) throws SQLException, DataAccessException {
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement st = connection.prepareStatement(
                     "UPDATE PLAYERS SET discord_id = ? WHERE uuid = ?")) {
            st.setLong(1, discordID);
            st.setString(2, uuid.toString());
            int affected = st.executeUpdate();
            if (affected == 0) {
                throw new DataAccessException("Your uuid was not found in the database. Contact developers!");
            }
        }
    }
}
