package me.colormaestro.taskmanager;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.sql.*;

public class ApproveTask implements CommandExecutor {
    private final FileConfiguration config;

    public ApproveTask(FileConfiguration config) {
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

            int project_id = config.getInt("project_id");

            conn = DriverManager.getConnection(url);

            Statement stmt = conn.createStatement();

            if (args.length > 1 && args[1].equalsIgnoreCase("force")) {
                String sql = "UPDATE tasks SET is_active = 0 WHERE " +
                        "id = " + args[0] + " AND project_id = " + project_id + ";";
                int affected = stmt.executeUpdate(sql);
                if (affected != 0) {
                    p.sendMessage(ChatColor.GREEN + "Task approved.");
                } else {
                    p.sendMessage(ChatColor.RED + "Task out of project scope. Make sure you are hitting valid task!");
                }
            } else {
                String sql = "UPDATE tasks SET is_active = 0 WHERE " +
                        "id = " + args[0] + " AND project_id = " + project_id + " AND color_id = 'green';";
                int affected = stmt.executeUpdate(sql);
                if (affected != 0) {
                    p.sendMessage(ChatColor.GREEN + "Task approved.");
                } else {
                    p.sendMessage(ChatColor.RED + "Cant approve unfinished task. If you want to approve despite task " +
                            "completion state, add \"force\" as second argument to this command");
                }
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
