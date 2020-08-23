package me.colormaestro.taskmanager;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.sql.*;

public class SetTaskPlace implements CommandExecutor {
    private final FileConfiguration config;

    public SetTaskPlace(FileConfiguration config) {
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

            String userName = config.getString("translation_table." + p.getName() + ".username");

            conn = DriverManager.getConnection(url);

            Statement stmt = conn.createStatement();

            // find creator_id according to project_id
            ResultSet rs_assignee = stmt.executeQuery( "SELECT id FROM users WHERE username = '" + userName + "';");

            if (rs_assignee.isClosed()) {
                p.sendMessage(ChatColor.RED + "ResultSet is empty, check the project_id in config file, also this " +
                        "can happen when player changes his username in MC or KB and forget to update the config");
            } else {
                Location location = p.getLocation();
                double x = location.getX();
                double y = location.getY();
                double z = location.getZ();
                float yaw = location.getYaw();
                float pitch = location.getPitch();
                int assignee_id = rs_assignee.getInt("id");
                String sql = "UPDATE tasks SET x = " + x + ", y = " + y + ", z = " + z + ", yaw = " + yaw +
                        ", pitch = " + pitch + " WHERE id = " + args[0] + " AND owner_id = " + assignee_id + ";";
                int affected = stmt.executeUpdate(sql);
                if (affected != 0) {
                    p.sendMessage(ChatColor.GREEN + "Workplace set.");
                } else {
                    p.sendMessage(ChatColor.RED + "Nothing updated. Make sure you are not setting place for other " +
                            "players' tasks!");
                }
                rs_assignee.close();
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
