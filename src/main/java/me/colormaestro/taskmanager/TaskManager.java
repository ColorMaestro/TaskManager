package me.colormaestro.taskmanager;

import me.colormaestro.taskmanager.commands.AddMember;
import me.colormaestro.taskmanager.commands.AddTask;
import me.colormaestro.taskmanager.commands.ApproveTask;
import me.colormaestro.taskmanager.commands.AssignTask;
import me.colormaestro.taskmanager.commands.Dashboard;
import me.colormaestro.taskmanager.commands.Establish;
import me.colormaestro.taskmanager.commands.FinishTask;
import me.colormaestro.taskmanager.commands.LinkDiscord;
import me.colormaestro.taskmanager.commands.NeedTasks;
import me.colormaestro.taskmanager.commands.PrepareTask;
import me.colormaestro.taskmanager.commands.RemoveMember;
import me.colormaestro.taskmanager.commands.ReturnTask;
import me.colormaestro.taskmanager.commands.SetTaskPlace;
import me.colormaestro.taskmanager.commands.TaskInfo;
import me.colormaestro.taskmanager.commands.Tasks;
import me.colormaestro.taskmanager.commands.TransferTask;
import me.colormaestro.taskmanager.commands.VisitTask;
import me.colormaestro.taskmanager.data.MemberDAO;
import me.colormaestro.taskmanager.data.TaskDAO;
import me.colormaestro.taskmanager.integrations.DecentHologramsIntegration;
import me.colormaestro.taskmanager.integrations.DecentHologramsOperator;
import me.colormaestro.taskmanager.integrations.DiscordOperator;
import me.colormaestro.taskmanager.integrations.DynmapIntegration;
import me.colormaestro.taskmanager.integrations.DynmapOperator;
import me.colormaestro.taskmanager.integrations.EmptyOperator;
import me.colormaestro.taskmanager.listeners.BookEditListener;
import me.colormaestro.taskmanager.listeners.PlayerJoinListener;
import me.colormaestro.taskmanager.listeners.inventory.ActiveTasksViewListener;
import me.colormaestro.taskmanager.listeners.inventory.ApprovedTasksViewListener;
import me.colormaestro.taskmanager.listeners.inventory.DashboardViewListener;
import me.colormaestro.taskmanager.listeners.inventory.IdleTaskViewListener;
import me.colormaestro.taskmanager.listeners.inventory.NeedTasksViewListener;
import me.colormaestro.taskmanager.listeners.inventory.PreparedTasksViewListener;
import me.colormaestro.taskmanager.listeners.inventory.SelectMemberListener;
import me.colormaestro.taskmanager.listeners.inventory.SupervisedTasksViewListener;
import me.colormaestro.taskmanager.scheduler.ProductionScheduler;
import me.colormaestro.taskmanager.scheduler.Scheduler;
import me.colormaestro.taskmanager.tabcompleters.MembersTabCompleter;
import me.colormaestro.taskmanager.tabcompleters.ReloadableTabCompleter;
import me.colormaestro.taskmanager.tabcompleters.TasksTabCompleter;
import me.colormaestro.taskmanager.utils.ItemStackCreator;
import me.colormaestro.taskmanager.utils.RunnablesCreator;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapAPI;

import java.util.Objects;

public final class TaskManager extends JavaPlugin {
    private FileConfiguration config;
    private TaskDAO taskDAO;
    private MemberDAO memberDAO;
    private DecentHologramsIntegration decentHolograms;
    private DynmapIntegration dynmap;

    @Override
    public void onEnable() {
        loadConfig();
        initDatabaseAccessors();
        resolveIntegrationsOperators();
        performBindingsSetup();
        DiscordOperator.instantiate(config.getString("token"), memberDAO, this);
    }

    @Override
    public void onDisable() {
        DiscordOperator.getInstance().shutdown();
    }

    private void loadConfig() {
        saveDefaultConfig();
        config = getConfig();
    }

    private void initDatabaseAccessors() {
        memberDAO = new MemberDAO(getDataFolder().getAbsolutePath());
        taskDAO = new TaskDAO(getDataFolder().getAbsolutePath());
    }

    private void resolveIntegrationsOperators() {
        if (Bukkit.getPluginManager().isPluginEnabled("DecentHolograms")) {
            this.getLogger().info("DecentHolograms plugin detected");
            decentHolograms = new DecentHologramsOperator();
        } else {
            this.getLogger().info("DecentHolograms plugin was not detected");
            decentHolograms = new EmptyOperator();
        }

        if (Bukkit.getPluginManager().getPlugin("dynmap") instanceof DynmapAPI dynmapAPI) {
            this.getLogger().info("Dynmap plugin detected");
            dynmap = new DynmapOperator(dynmapAPI);
        } else {
            this.getLogger().info("Dynmap plugin was not detected");
            dynmap = new EmptyOperator();
        }
    }

    private void performBindingsSetup() {
        ReloadableTabCompleter tasksTabCompleter = new TasksTabCompleter(memberDAO);
        ReloadableTabCompleter membersTabCompleter = new MembersTabCompleter(memberDAO);
        setTabCompleter("tasks", tasksTabCompleter);
        setTabCompleter("addtask", membersTabCompleter);
        setTabCompleter("dashboard", membersTabCompleter);

        Scheduler scheduler = new ProductionScheduler(this);
        ItemStackCreator stackCreator = new ItemStackCreator(this);
        RunnablesCreator creator = new RunnablesCreator(scheduler, this, stackCreator, taskDAO, memberDAO, decentHolograms);

        registerEventListener(new PlayerJoinListener(this, taskDAO, memberDAO, tasksTabCompleter, membersTabCompleter, decentHolograms));
        registerEventListener(new BookEditListener(this, taskDAO, memberDAO, decentHolograms, dynmap));
        registerEventListener(new DashboardViewListener(scheduler, creator, stackCreator));
        registerEventListener(new SupervisedTasksViewListener(scheduler, creator));
        registerEventListener(new ActiveTasksViewListener(scheduler, creator, stackCreator));
        registerEventListener(new ApprovedTasksViewListener(scheduler, creator));
        registerEventListener(new PreparedTasksViewListener(scheduler, creator));
        registerEventListener(new IdleTaskViewListener(scheduler, creator));
        registerEventListener(new NeedTasksViewListener(scheduler, creator));
        registerEventListener(new SelectMemberListener(scheduler, creator));

        setCommandExecutor("addmember", new AddMember(scheduler, memberDAO, tasksTabCompleter, membersTabCompleter));
        setCommandExecutor("removemember", new RemoveMember(scheduler, memberDAO, tasksTabCompleter, membersTabCompleter));
        setCommandExecutor("dashboard", new Dashboard(scheduler, creator));
        setCommandExecutor("tasks", new Tasks(scheduler, this, taskDAO, memberDAO));
        setCommandExecutor("addtask", new AddTask(scheduler, stackCreator, taskDAO));
        setCommandExecutor("preparetask", new PrepareTask(stackCreator));
        setCommandExecutor("assigntask", new AssignTask(scheduler, creator));
        setCommandExecutor("finishtask", new FinishTask(scheduler, taskDAO, memberDAO, decentHolograms, dynmap));
        setCommandExecutor("approvetask", new ApproveTask(scheduler, taskDAO, memberDAO, decentHolograms, dynmap));
        setCommandExecutor("visittask", new VisitTask(scheduler, creator));
        setCommandExecutor("returntask", new ReturnTask(scheduler, taskDAO, memberDAO, decentHolograms));
        setCommandExecutor("settaskplace", new SetTaskPlace(scheduler, taskDAO, memberDAO));
        setCommandExecutor("linkdiscord", new LinkDiscord());
        setCommandExecutor("establish", new Establish(scheduler, taskDAO, memberDAO, decentHolograms));
        setCommandExecutor("taskinfo", new TaskInfo(scheduler, creator));
        setCommandExecutor("transfertask", new TransferTask(scheduler, taskDAO, memberDAO, decentHolograms));
        setCommandExecutor("needtasks", new NeedTasks(scheduler, this, taskDAO));
    }

    private void registerEventListener(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }

    private void setCommandExecutor(String command, CommandExecutor executor) {
        Objects.requireNonNull(this.getCommand(command)).setExecutor(executor);
    }

    private void setTabCompleter(String command, TabCompleter tabCompleter) {
        Objects.requireNonNull(this.getCommand(command)).setTabCompleter(tabCompleter);
    }
}
