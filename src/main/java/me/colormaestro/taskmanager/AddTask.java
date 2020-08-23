package me.colormaestro.taskmanager;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.sql.*;

public class AddTask implements CommandExecutor {
    private final FileConfiguration config;

    public AddTask(FileConfiguration config) {
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command cant be run from console.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "You must provide description for task.");
            return true;
        }

        Connection conn = null;
        Player p = (Player) sender;
        try {
            Class.forName("org.sqlite.JDBC");

            String path = config.getString("db_file_path");
            String url = "jdbc:sqlite:" + path;

            String columnName = config.getString("translation_table." + args[0] + ".column");
            String assigneeName = config.getString("translation_table." + args[0] + ".username");
            String creatorName = config.getString("translation_table." + p.getName() + ".username");
            if (columnName == null) {
                p.sendMessage(ChatColor.RED + "Invalid username specified, if you believe the username is " +
                        "correct, check the translation table in config file");
                return true;
            }
            int project_id = config.getInt("project_id");
            StringBuilder title = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                title.append(args[i]).append(" ");
            }
            title = new StringBuilder(title.substring(0, title.length() - 1));

            conn = DriverManager.getConnection(url);

            // find column according to username and project_id
            Statement stmt1 = conn.createStatement();
            ResultSet rs_column = stmt1.executeQuery( "SELECT id FROM columns WHERE " +
                    "title = '" + columnName + "' AND project_id = " + project_id + ";");

            // find swimlane according to project_id
            Statement stmt2 = conn.createStatement();
            ResultSet rs_swim = stmt2.executeQuery( "SELECT id FROM swimlanes WHERE " +
                    "project_id = " + project_id + " AND name = 'Default swimlane';");

            // find creator according to KB username
            Statement stmt3 = conn.createStatement();
            ResultSet rs_creator = stmt3.executeQuery( "SELECT id FROM users WHERE username = '" + creatorName + "';");

            // find assignee according to KB username
            Statement stmt4 = conn.createStatement();
            ResultSet rs_assignee = stmt4.executeQuery( "SELECT id FROM users WHERE username = '" + assigneeName + "';");

            if (rs_column.isClosed() || rs_swim.isClosed() || rs_creator.isClosed() || rs_assignee.isClosed()) {
                p.sendMessage(ChatColor.RED + "ResultSet is empty, check the project_id in config file, also this " +
                        "can happen when player changes his username in MC or KB and forget to update the config");
            } else {
                int column_id = rs_column.getInt("id");
                long time = System.currentTimeMillis() / 1000;
                int swimlane_id = rs_swim.getInt("id");
                int creator_id = rs_creator.getInt("id");
                int assignee_id = rs_assignee.getInt("id");
                Location location = p.getLocation();
                double x = location.getX();
                double y = location.getY();
                double z = location.getZ();
                float yaw = location.getYaw();
                float pitch = location.getPitch();
                String sql = "INSERT INTO tasks " +
                    "(title, description, date_creation, color_id, project_id, column_id, owner_id, position, " +
                    "score, date_due, creator_id, date_modification, date_started, swimlane_id, date_moved, x, y, z, yaw, pitch) " +
                    "VALUES ('" + title + "', '', '" + time + "', 'orange', " + project_id + ", " + column_id + ", "
                        + assignee_id + ", 1, 0, 0, " + creator_id + ", " + time + ", 0, " + swimlane_id + ", " +
                        time + ", " + x + ", " + y + ", " + z + ", " + yaw + ", " + pitch + ");";
                stmt1.executeUpdate(sql);
                rs_column.close();
                rs_swim.close();
                rs_creator.close();
            }
            stmt1.close();
            stmt2.close();
            stmt3.close();
            stmt4.close();
            p.sendMessage(ChatColor.GREEN + "Task added.");
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
            p.sendMessage(ChatColor.RED + e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
                p.sendMessage(ChatColor.DARK_RED + ex.getMessage());
            }
        }
        return true;
    }
}
