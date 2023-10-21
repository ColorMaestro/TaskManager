package me.colormaestro.taskmanager.tabcompleters;

import me.colormaestro.taskmanager.data.MemberDAO;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MembersTabCompleter implements ReloadableTabCompleter {
    protected final MemberDAO memberDAO;
    protected List<String> names;

    /**
     * Provides names of members as suggested command completion.
     * @param memberDAO
     */
    public MembersTabCompleter(MemberDAO memberDAO) {
        this.memberDAO = memberDAO;
        reload();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (sender instanceof Player && args.length == 1) {
            return names.stream()
                    .filter(ign -> ign.toLowerCase().contains(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return null;
    }

    public void reload() {
        try {
            names = memberDAO.getMembersNames();
        } catch (SQLException e) {
            e.printStackTrace();
            names = new ArrayList<>();
        }
    }
}
