package me.colormaestro.taskmanager.data;

import me.colormaestro.taskmanager.enums.TaskStatus;
import me.colormaestro.taskmanager.model.MemberTaskStats;
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
                    "title VARCHAR(40) NOT NULL," +
                    "description VARCHAR(200) NOT NULL," +
                    "assignee_id INT NOT NULL," +
                    "advisor_id INT NOT NULL," +
                    "x DOUBLE," +
                    "y DOUBLE," +
                    "z DOUBLE," +
                    "yaw DOUBLE," +
                    "pitch DOUBLE," +
                    "status VARCHAR(10)," +
                    "date_given DATE," +
                    "date_finished DATE," +
                    "FOREIGN KEY (assignee_id) REFERENCES PLAYERS (id)," +
                    "FOREIGN KEY (advisor_id) REFERENCES PLAYERS (id)" +
                    ")");
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to create TASKS table", ex);
        }
    }

    /**
     * Creates new task.
     *
     * @param task task to create
     * @throws SQLException if the task has already set ID
     */
    public synchronized void createTask(Task task) throws SQLException {
        if (task.getId() != null) {
            throw new IllegalArgumentException("Creating task with set ID");
        }
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement st = connection.prepareStatement(
                     "INSERT INTO TASKS (title, description, assignee_id, advisor_id, x, y, z, yaw, pitch, " +
                             "status, date_given, date_finished) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            st.setString(1, task.getTitle());
            st.setString(2, task.getDescription());
            st.setInt(3, task.getAssigneeID());
            st.setInt(4, task.getAdvisorID());
            st.setDouble(5, task.getX());
            st.setDouble(6, task.getY());
            st.setDouble(7, task.getZ());
            st.setFloat(8, task.getYaw());
            st.setFloat(9, task.getPitch());
            st.setString(10, task.getStatus().name());
            st.setDate(11, new Date(System.currentTimeMillis()));
            st.setDate(12, null);
            st.executeUpdate();
        }
    }

    /**
     * Marks task as finished.
     *
     * @param taskID     id of the task
     * @param assigneeID id of assignee
     * @throws SQLException        if SQL error arise
     * @throws DataAccessException if player tries to finish task which belongs to someone else
     */
    public synchronized void finishTask(int taskID, int assigneeID) throws SQLException, DataAccessException {
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement st = connection.prepareStatement(
                     "UPDATE TASKS SET status = 'FINISHED', date_finished = ? WHERE id = ? AND " +
                             "assignee_id = ? AND status = 'DOING'")) {

            st.setDate(1, new Date(System.currentTimeMillis()));
            st.setInt(2, taskID);
            st.setInt(3, assigneeID);
            int affected = st.executeUpdate();
            if (affected == 0) {
                throw new DataAccessException("No change. Make sure you choose your existing task and not completed yet.");
            }
        }
    }

    /**
     * Sets task state to {@link me.colormaestro.taskmanager.enums.TaskStatus#DOING}, which is the default for new tasks.
     *
     * @param id    ID of the task
     * @param force whether to proceed if the task is approved
     * @throws SQLException        if SQL error arise
     * @throws DataAccessException if task does not exist or if it's already approved and force option is not used
     */
    public synchronized void returnTask(int id, boolean force) throws SQLException, DataAccessException {
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT status FROM TASKS WHERE id = ?"
             );
             PreparedStatement st = connection.prepareStatement(
                     "UPDATE TASKS SET status = 'DOING' WHERE id = ?")) {

            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();
            if (rs.isClosed()) {
                throw new DataAccessException("No task with such an ID found.");
            }
            TaskStatus status = TaskStatus.valueOf(rs.getString("status"));
            if (status == TaskStatus.APPROVED && !force) {
                throw new DataAccessException("The task is approved. If you want to proceed add force " +
                        "as second argument to this command");
            }
            rs.close();

            st.setInt(1, id);
            int affected = st.executeUpdate();
            if (affected == 0) {
                throw new DataAccessException("No change. Make sure you choose not returned task yet.");
            }
        }
    }

    /**
     * Sets task state to {@link me.colormaestro.taskmanager.enums.TaskStatus#APPROVED}.
     *
     * @param id    id of the task
     * @param force whether to proceed if the task is not finished
     * @throws SQLException        if SQL error arise
     * @throws DataAccessException if task does not exist or if it's not finished yet and force option is not used
     */
    public synchronized void approveTask(int id, boolean force) throws SQLException, DataAccessException {
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT status FROM TASKS WHERE id = ?"
             );
             PreparedStatement st = connection.prepareStatement(
                     "UPDATE TASKS SET status = 'APPROVED' WHERE id = ?")) {

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

    /**
     * Finds task according to its ID.
     *
     * @throws SQLException        if SQL error arise
     * @throws DataAccessException if there's no task with such ID
     */
    public synchronized Task findTask(int id) throws SQLException, DataAccessException {
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement st = connection.prepareStatement(
                     "SELECT title, description, assignee_id, advisor_id, x, y, z, yaw, pitch, " +
                             "status, date_given, date_finished FROM TASKS WHERE id = ?")) {

            st.setInt(1, id);
            ResultSet rs = st.executeQuery();
            if (rs.isClosed()) {
                throw new DataAccessException("No task found with id " + id);
            }
            Task task = new Task(
                    rs.getString("title"),
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
            task.setId(id);
            return task;
        }
    }

    /**
     * Fetches all tasks from database.
     *
     * @return list of all tasks
     * @throws SQLException if SQL error arise
     */
    public synchronized List<Task> fetchAllTasks() throws SQLException {
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement st = connection.prepareStatement(
                     "SELECT id, title, description, assignee_id, advisor_id, x, y, z, yaw, pitch, status, " +
                             "date_given, date_finished FROM TASKS")) {

            ResultSet rs = st.executeQuery();
            List<Task> tasks = new ArrayList<>();
            while (rs.next()) {
                Task task = new Task(
                        rs.getString("title"),
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

    /**
     * Retrieves all active tasks (status {@link me.colormaestro.taskmanager.enums.TaskStatus#DOING} or
     * {@link me.colormaestro.taskmanager.enums.TaskStatus#FINISHED}) of selected person. Used typically on updating
     * hologram task list.
     *
     * @param assigneeID id of assignee
     * @return active (given and finished, not approved) tasks on which is assignee currently working
     * @throws SQLException if SQL error arise
     */
    public synchronized List<Task> fetchPlayersActiveTasks(int assigneeID) throws SQLException {
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement st = connection.prepareStatement(
                     "SELECT id, title, description, assignee_id, advisor_id, x, y, z, yaw, pitch, status, " +
                             "date_given, date_finished FROM TASKS WHERE assignee_id = ? AND status != 'APPROVED'")) {

            st.setInt(1, assigneeID);
            ResultSet rs = st.executeQuery();
            List<Task> tasks = new ArrayList<>();
            while (rs.next()) {
                Task task = new Task(
                        rs.getString("title"),
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

    /**
     * Retrieves all finished tasks whose advisor is selected person. Used typically for checking whether there are some
     * finished tasks to review.
     *
     * @param advisorID id of advisor
     * @return finished tasks, in which the player figures as advisor
     * @throws SQLException if SQL error arise
     */
    public synchronized List<Task> fetchFinishedTasks(int advisorID) throws SQLException {
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement st = connection.prepareStatement(
                     "SELECT id, title, description, assignee_id, advisor_id, x, y, z, yaw, pitch, status, " +
                             "date_given, date_finished FROM TASKS WHERE advisor_id = ? AND status = 'FINISHED'")) {

            st.setInt(1, advisorID);
            ResultSet rs = st.executeQuery();
            List<Task> tasks = new ArrayList<>();
            while (rs.next()) {
                Task task = new Task(
                        rs.getString("title"),
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

    /**
     * Retrieves all active tasks (status {@link me.colormaestro.taskmanager.enums.TaskStatus#DOING} or
     * {@link me.colormaestro.taskmanager.enums.TaskStatus#FINISHED}) whose advisor is selected person.
     * This help advisors to check which tasks are still active.
     * param advisor id of advisor
     *
     * @return Tasks which were given by this advisor and are not approved yet.
     * @throws SQLException if SQL error arise
     */
    public synchronized List<Task> fetchAdvisorActiveTasks(int advisorID) throws SQLException {
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement st = connection.prepareStatement(
                     "SELECT id, title, description, assignee_id, advisor_id, x, y, z, yaw, pitch, status, " +
                             "date_given, date_finished FROM TASKS WHERE advisor_id = ? AND status != 'APPROVED'")) {

            st.setInt(1, advisorID);
            ResultSet rs = st.executeQuery();
            List<Task> tasks = new ArrayList<>();
            while (rs.next()) {
                Task task = new Task(
                        rs.getString("title"),
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

    /**
     * Updates task coordinates (location). Only assignee and advisor of the task have this ability.
     *
     * @param taskID     ID of the task, in which to update coordinates
     * @param assigneeID ID of assignee
     * @param location   new location for the task
     * @throws SQLException        if SQL error arise
     * @throws DataAccessException if there's no such task or player selects task of another person.
     */
    public synchronized void updateTaskCords(int taskID, int assigneeID, Location location) throws SQLException, DataAccessException {
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement st = connection.prepareStatement(
                     "UPDATE TASKS SET x = ?, y = ?, z = ?, yaw = ?, pitch = ? WHERE id = ? AND assignee_id = ?")) {

            st.setDouble(1, location.getX());
            st.setDouble(2, location.getY());
            st.setDouble(3, location.getZ());
            st.setFloat(4, location.getYaw());
            st.setFloat(5, location.getPitch());
            st.setInt(6, taskID);
            st.setInt(7, assigneeID);
            int affected = st.executeUpdate();
            if (affected == 0) {
                throw new DataAccessException("No change. Make sure you choose your existing task.");
            }
        }
    }

    /**
     * Updates task assignee. Useful when advisors want to transfer existing task to someone else.
     *
     * @param taskID     ID of the task
     * @param assigneeID ID of new assignee
     * @throws SQLException        if SQL error arise
     * @throws DataAccessException if the task does not exist
     */
    public synchronized void updateTaskAssignee(int taskID, int assigneeID) throws SQLException, DataAccessException {
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement st = connection.prepareStatement(
                     "UPDATE TASKS SET assignee_id = ? WHERE id = ?")) {

            st.setInt(1, assigneeID);
            st.setInt(2, taskID);
            int affected = st.executeUpdate();
            if (affected == 0) {
                throw new DataAccessException("No change. Make sure you choose valid task.");
            }
        }
    }

    /**
     * Collects tasks statistics about members e.g. who has how many tasks of each {@link me.colormaestro.taskmanager.enums.TaskStatus}
     *
     * @return List of stats for each player
     * @throws SQLException if SQL error arise
     */
    public synchronized List<MemberTaskStats> fetchTaskStatistics() throws SQLException {
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement st = connection.prepareStatement(
                     """
                             select
                               ign,
                               count(tasks.id) filter (where status = 'DOING') as "doing",
                               count(tasks.id) filter (where status = 'FINISHED') as "finished",
                               count(tasks.id) filter (where status = 'APPROVED') as "approved"
                             from players inner join tasks on players.id = tasks.assignee_id
                             group by ign
                             order by ign""")) {
            ResultSet rs = st.executeQuery();
            List<MemberTaskStats> stats = new ArrayList<>();
            while (rs.next()) {
                MemberTaskStats memberTaskStats = new MemberTaskStats(
                        rs.getString("ign"),
                        rs.getInt("doing"),
                        rs.getInt("finished"),
                        rs.getInt("approved")
                );
                stats.add(memberTaskStats);
            }
            rs.close();
            return stats;
        }
    }
}
