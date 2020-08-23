package me.colormaestro.taskmanager;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.sql.*;

public class ReturnTask implements CommandExecutor {
    private final FileConfiguration config;

    public ReturnTask(FileConfiguration config) {
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command cant be run from console.");
            return true;
        }

        if (args.length < 2) {
            if (args.length == 0) {
                sender.sendMessage(ChatColor.RED + "You must specify task_id.");
            } else {
                sender.sendMessage(ChatColor.RED + "You must specify reason.");
            }
            return true;
        }

        Connection conn = null;
        Player p = (Player) sender;
        try {
            Class.forName("org.sqlite.JDBC");

            String path = config.getString("db_file_path");
            String url = "jdbc:sqlite:" + path;
            StringBuilder reason = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                reason.append(args[i]).append(" ");
            }
            reason = new StringBuilder(reason.substring(0, reason.length() - 1));

            conn = DriverManager.getConnection(url);

            Statement stmt = conn.createStatement();

            String sql = "UPDATE tasks SET color_id = 'red', description = '" + reason + "' WHERE " +
                    "id = " + args[0] + " AND is_active = 1 AND color_id = 'green';";
            int affected = stmt.executeUpdate(sql);
            if (affected != 0) {
                p.sendMessage(ChatColor.GREEN + "Task returned.");
            } else {
                p.sendMessage(ChatColor.RED + "Nothing updated. Make sure you are hitting finished task!");
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
