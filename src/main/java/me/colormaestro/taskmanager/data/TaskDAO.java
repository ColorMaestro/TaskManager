package me.colormaestro.taskmanager.data;

import me.colormaestro.taskmanager.enums.TaskStatus;
import me.colormaestro.taskmanager.model.Task;
import org.bukkit.Location;

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

public class TaskDAO {
    private final String url;

    public TaskDAO(String dataFolderPath) {
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
             ResultSet rs = connection.getMetaData().getTables(null, null, "TASKS", null)) {
            return rs.next();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to detect if the table TASKS exist", ex);
        }
    }

    private void createTable() {
        try (Connection connection = DriverManager.getConnection(url);
             Statement st = connection.createStatement()) {

            st.executeUpdate("CREATE TABLE TASKS (" +
                    "id INTEGER PRIMARY KEY," +
                    "description VARCHAR(100) NOT NULL," +
                    "assignee_id INT NOT NULL," +
                    "advisor_id INT NOT NULL," +
                    "x DOUBLE," +
                    "y DOUBLE," +
                    "z DOUBLE," +
                    "yaw DOUBLE," +
                    "pitch DOUBLE," +
                    "status VARCHAR(10)," +
                    "date_given DATE," +
                    "date_finished DATE" +
                    ")");
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to create TASKS table", ex);
        }
    }

    public synchronized void createTask(Task task) throws SQLException {
        if (task.getId() != null) {
            throw new IllegalArgumentException("Creating task with set ID");
        }
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement st = connection.prepareStatement(
                     "INSERT INTO TASKS (description, assignee_id, advisor_id, x, y, z, yaw, pitch, " +
                             "status, date_given, date_finished) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            st.setString(1, task.getDescription());
            st.setInt(2, task.getAssigneeID());
            st.setInt(3, task.getAdvisorID());
            st.setDouble(4, task.getX());
            st.setDouble(5, task.getY());
            st.setDouble(6, task.getZ());
            st.setFloat(7, task.getYaw());
            st.setFloat(8, task.getPitch());
            st.setString(9, task.getStatus().name());
            st.setDate(10, new Date(System.currentTimeMillis()));
            st.setDate(11, null);
            st.executeUpdate();
        }
    }

    public synchronized void finishTask(int id, int assignee) throws SQLException, DataAccessException {
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement st = connection.prepareStatement(
                     "UPDATE TASKS SET status = 'finished', date_finished = ? WHERE id = ? AND assignee_id = ?")) {

            st.setDate(1, new Date(System.currentTimeMillis()));
            st.setInt(2, id);
            st.setInt(3, assignee);
            int affected = st.executeUpdate();
            if (affected == 0) {
                throw new DataAccessException("No change. Make sure you choose your task and not completed yet.");
            }
        }
    }

    public synchronized void approveTask(int id, boolean force) throws SQLException, DataAccessException {
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT status FROM TASKS WHERE id = ?"
             );
             PreparedStatement st = connection.prepareStatement(
                     "UPDATE TASKS SET status = 'approved' WHERE id = ?")) {

            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();
            if (rs.isClosed()) {
                throw new DataAccessException("No task with such an ID found.");
            }
            TaskStatus status = TaskStatus.valueOf(rs.getString("status"));
            if (status == TaskStatus.DOING && !force) {
                throw new DataAccessException("The task is still in progress. If you want to proceed add force " +
                        "as second argument to this command");
            }
            rs.close();

            st.setInt(1, id);
            int affected = st.executeUpdate();
            if (affected == 0) {
                throw new DataAccessException("No change. Make sure you choose not accepted task yet.");
            }
        }
    }

    public synchronized Task findTask(int id) throws SQLException, DataAccessException {
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement st = connection.prepareStatement(
                     "SELECT description, assignee_id, advisor_id, x, y, z, yaw, pitch, " +
                             "status, date_given, date_finished FROM TASKS WHERE id = ?")) {

            st.setInt(1, id);
            ResultSet rs = st.executeQuery();
            if (rs.isClosed()) {
                throw new DataAccessException("No task found with id " + id);
            }
            Task task = new Task(
                    rs.getString("description"),
                    rs.getInt("assignee_id"),
                    rs.getInt("advisor_id"),
                    rs.getDouble("x"),
                    rs.getDouble("y"),
                    rs.getDouble("z"),
                    rs.getFloat("yaw"),
                    rs.getFloat("pitch"),
                    TaskStatus.valueOf(rs.getString("status")),
                    rs.getDate("date_given"),
                    rs.getDate("date_finished")
            );
            rs.close();
            return task;
        }
    }

    public synchronized List<Task> fetchPlayersActiveTasks(int assignee) throws SQLException {
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement st = connection.prepareStatement(
                     "SELECT id, description, assignee_id, advisor_id, x, y, z, yaw, pitch, status, " +
                             "date_given, date_finished FROM TASKS WHERE assignee_id = ? AND status != 'approved'")) {

            st.setInt(1, assignee);
            ResultSet rs = st.executeQuery();
            List<Task> tasks = new ArrayList<>();
            while (rs.next()) {
                Task task = new Task(
                        rs.getString("description"),
                        rs.getInt("assignee_id"),
                        rs.getInt("advisor_id"),
                        rs.getDouble("x"),
                        rs.getDouble("y"),
                        rs.getDouble("z"),
                        rs.getFloat("yaw"),
                        rs.getFloat("pitch"),
                        TaskStatus.valueOf(rs.getString("status")),
                        rs.getDate("date_given"),
                        rs.getDate("date_finished")
                );
                task.setId(rs.getInt("id"));
                tasks.add(task);
            }
            rs.close();
            return tasks;
        }
    }

    public synchronized void updateTaskCords(int id, int assignee, Location location) throws SQLException, DataAccessException {
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement st = connection.prepareStatement(
                     "UPDATE TASKS SET x = ?, y = ?, z = ?, yaw = ?, pitch = ? WHERE id = ? AND assignee_id = ?")) {

            st.setDouble(1, location.getX());
            st.setDouble(2, location.getY());
            st.setDouble(3, location.getZ());
            st.setFloat(4, location.getYaw());
            st.setFloat(5, location.getPitch());
            st.setInt(6, id);
            st.setInt(7, assignee);
            int affected = st.executeUpdate();
            if (affected == 0) {
                throw new DataAccessException("No change. Make sure you choose your existing task.");
            }
        }
    }
}
