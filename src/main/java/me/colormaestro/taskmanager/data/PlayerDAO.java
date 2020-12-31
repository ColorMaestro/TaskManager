package me.colormaestro.taskmanager.data;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
            addRecords();
        }
    }

    private void addRecords() {
        try (Connection connection = DriverManager.getConnection(url);
             Statement st = connection.createStatement()) {

            st.executeUpdate("INSERT INTO PLAYERS (uuid, ign, discord_id) VALUES " +
                    "('3bfe0fa3-033a-421d-839b-5b450c37ead4', 'AWEJOKER', '347783781119164418')," +
                    "('2160922f-b271-498f-b092-a0a245a15132', 'Arturke44', '428803117702971393')," +
                    "('ff449188-20bb-4136-a44d-14e6109c868f', 'Bubblegum_Wolf', '425070690668904448')," +
                    "('b6219cc2-16c2-4d89-8d06-a38beb2cf908', 'ColorMaestro', '270265573374427137')," +
                    "('df725209-190c-45ca-aed3-fd2734aa3a83', 'ElasticWaistband', '536663959923589120')," +
                    "('74665f98-a253-4d3a-8527-d9c3736f1745', 'Erwa94', '247014055154286592')," +
                    "('40108c9c-ed3c-4807-af48-a8d9ee91fed7', 'Gazur1223', '295860806761971712')," +
                    "('9c428d31-7a97-4e60-abf1-c8e9b71368ff', 'KoganeNoKenshi', '238318052331094026')," +
                    "('b48633e7-fa77-4a61-93f9-f22a997c1905', 'MrSpy321', '284084811436654592')," +
                    "('07f8baa2-e8ef-4221-b4bb-8454240d2984', 'Picorims', '281446615024271372')," +
                    "('d2be1846-7fe9-4370-918a-5d2a43c06ac2', 'RekNepZ_HBK', '134022395152302080')," +
                    "('f79a6422-bbaf-4419-ab21-28293c4fdfc1', 'Russicat', '320469645410697217')," +
                    "('7192fc1f-383e-4aae-8c8f-987eaebacfe8', 'Xoyjaz', '268803273320824833')," +
                    "('05046a05-ad42-4880-b928-924ea388267b', 'XxTechnoAngelxX', '336895126913679361')," +
                    "('c9eb4c08-ed8b-4865-919c-aacbcf68ab10', 'evsh777', '309748213714386945')," +
                    "('afe6ff86-dc63-4d07-aa9f-c11b812166b5', 'Sir_Palo', '213093653038628864')," +
                    "('09f50998-f3b6-4655-9e28-7b730346009d', 'Baz100', '247172685329727490')"
            );
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
                throw new DataAccessException("Your uuid was not found in the database. Contact project manager!");
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
}
