package me.colormaestro.taskmanager;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.sql.*;

public class VisitTask implements CommandExecutor {
    private final FileConfiguration config;

    public VisitTask(FileConfiguration config) {
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command cant be run from console.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "You must specify task_id.");
            return true;
        }

        Connection conn = null;
        Player p = (Player) sender;
        try {
            Class.forName("org.sqlite.JDBC");

            String path = config.getString("db_file_path");
            String url = "jdbc:sqlite:" + path;

            String task_id = args[0];
            int project_id = config.getInt("project_id");

            conn = DriverManager.getConnection(url);

            // find column according to username and project_id
            Statement stmt = conn.createStatement();
            ResultSet rs_column = stmt.executeQuery( "SELECT x, y, z, yaw, pitch FROM tasks WHERE " +
                    "id = " + task_id + " AND project_id = " + project_id + ";");

            if (rs_column.isClosed()) {
                p.sendMessage(ChatColor.RED + "No task with such id found in this project!");
            } else {
                double x = rs_column.getDouble("x");
                double y = rs_column.getDouble("y");
                double z = rs_column.getDouble("z");
                float yaw = rs_column.getFloat("yaw");
                float pitch = rs_column.getFloat("pitch");
                Location location = new Location(p.getWorld(), x, y, z, yaw, pitch);
                p.teleport(location);
                rs_column.close();
            }
            stmt.close();
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
