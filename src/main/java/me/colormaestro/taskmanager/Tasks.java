package me.colormaestro.taskmanager;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.sql.*;

public class Tasks implements CommandExecutor {
    private final FileConfiguration config;

    public Tasks(FileConfiguration config) {
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (args.length == 1 && args[0].equals("help")) {
            ChatColor g = ChatColor.GOLD;
            ChatColor w = ChatColor.WHITE;
            sender.sendMessage(ChatColor.AQUA + "-=-=-=-=-=- TaskManager help -=-=-=-=-=-");
            sender.sendMessage(g + "/tasks help" + w + " - shows this help");
            sender.sendMessage(g + "/tasks [IGN]" + w + " - shows your or other player tasks");
            sender.sendMessage(g + "/visittask <id>" + w + " - teleports to the task workplace");
            sender.sendMessage(g + "/addtask <IGN> <description>" + w + " - adds new task");
            sender.sendMessage(g + "/finishtask <id>" + w + " - mark task as finished");
            sender.sendMessage(g + "/approvetask <id> [force]" + w + " - approves the finished task (with force also unfinished one)");
            sender.sendMessage(g + "/returntask <id> <reason>" + w + " - return finished task back to the unfinished state");
            sender.sendMessage(g + "/settaskplace <id>" + w + " - sets spawning point for this task for more comfort :)");
            return true;
        }

        if ((args.length == 0 && sender instanceof Player) || args.length == 1) {
            Player p = (Player) sender;
            Connection conn = null;
            try {
                Class.forName("org.sqlite.JDBC");

                String path = config.getString("db_file_path");
                String url = "jdbc:sqlite:" + path;

                String columnName = config.getString("translation_table." + p.getName() + ".column");
                if (args.length == 1) {
                    columnName = config.getString("translation_table." + args[0] + ".column");
                    if (columnName == null) {
                        p.sendMessage(ChatColor.RED + "Invalid username specified, if you believe the username is " +
                                "correct, check the translation table in config file");
                        return true;
                    }
                }
                int project_id = config.getInt("project_id");

                conn = DriverManager.getConnection(url);

                // find column according to username and project_id
                Statement stmt = conn.createStatement();
                ResultSet rs_column = stmt.executeQuery( "SELECT id FROM columns WHERE " +
                        "title = '" + columnName + "' AND project_id = " + project_id + ";");
                if (rs_column.isClosed()) {
                    p.sendMessage(ChatColor.RED + "ResultSet is empty, check the project_id in config file," +
                            " also this can happen when player changes his IGN and forget to update the config");
                } else {
                    int column_id = rs_column.getInt("id");
                    ResultSet tasks = stmt.executeQuery( "SELECT id, title, color_id FROM tasks WHERE " +
                            "column_id = " + column_id + " AND project_id = " + project_id + " AND is_active = 1;");
                    if (tasks.isClosed()) {
                        if (args.length == 1) {
                            p.sendMessage(ChatColor.GREEN + args[0] + " has no tasks!");
                        } else {
                            p.sendMessage(ChatColor.GREEN + "You have no tasks!");
                        }
                    } else {
                        if (args.length == 1) {
                            p.sendMessage(ChatColor.AQUA + "-=-=-=- " + args[0] + "'s tasks -=-=-=-");
                        } else {
                            p.sendMessage(ChatColor.AQUA + "-=-=-=- Your tasks -=-=-=-");
                        }
                        while (tasks.next()) {
                            int id = tasks.getInt("id");
                            String color = tasks.getString("color_id");
                            String title = tasks.getString("title");
                            switch (color) {
                                case "orange":
                                    p.sendMessage(ChatColor.GOLD + "[" + id + "] " + ChatColor.WHITE + title);
                                    break;
                                case "green":
                                    p.sendMessage(ChatColor.GREEN + "[" + id + "] " + ChatColor.WHITE + title);
                                    break;
                                case "red":
                                    p.sendMessage(ChatColor.RED + "[" + id + "] " + ChatColor.WHITE + title);
                                    break;
                                default:
                                    p.sendMessage("[" + id + "] " + title);
                            }
                        }
                        tasks.close();
                    }
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

        sender.sendMessage(ChatColor.RED + "Usage: /tasks [player] or /tasks help");
        return true;
    }
}
