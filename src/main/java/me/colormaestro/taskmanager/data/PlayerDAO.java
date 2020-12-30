package me.colormaestro.taskmanager.data;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
            addRecords();
        }
    }

    private void addRecords() {
        try (Connection connection = DriverManager.getConnection(url);
             var st = connection.createStatement()) {

            st.executeUpdate("INSERT INTO PLAYERS (uuid, ign, discord_id) VALUES " +
                    "('', 'Xoyjaz', 1)," +
                    "('', 'Xoyjaz', 2)," +
                    "('', 'Xoyjaz', 3)");
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to insert players data into PLAYERS table", ex);
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
                    "ign INT NOT NULL," +
                    "discord_id INT" +
                    ")");
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to create PLAYERS table", ex);
        }
    }
}
