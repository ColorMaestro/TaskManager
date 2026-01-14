package me.colormaestro.taskmanager.commands;

import me.colormaestro.taskmanager.FakeCommand;
import me.colormaestro.taskmanager.SendMessageAnswer;
import me.colormaestro.taskmanager.data.DataAccessException;
import me.colormaestro.taskmanager.data.MemberDAO;
import me.colormaestro.taskmanager.data.TaskDAO;
import me.colormaestro.taskmanager.enums.TaskStatus;
import me.colormaestro.taskmanager.model.AdvisedTask;
import me.colormaestro.taskmanager.model.IdleTask;
import me.colormaestro.taskmanager.model.Member;
import me.colormaestro.taskmanager.model.Task;
import me.colormaestro.taskmanager.scheduler.FakeScheduler;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TaskTest {
    private static final int MEMBER_ID = 1;
    private static final UUID MEMBER_UUID = new UUID(0L, 1L);
    private static final String PLAYER_NAME = "ColorMaestro";
    private static final String MEMBER_NAME = "JohnDoe";
    private static final String MEMBER_NAME_2 = "JaneSmith";
    private static final Member MEMBER = new Member(
            MEMBER_UUID.toString(),
            MEMBER_NAME,
            Date.valueOf(LocalDate.of(2026, 1, 1)),
            123L,
            true
    );
    private static final Command FAKE_COMMAND = new FakeCommand();
    private final MemberDAO memberDAOmock = mock(MemberDAO.class);
    private final TaskDAO taskDAOmock = mock(TaskDAO.class);
    private final Tasks tasks = new Tasks(new FakeScheduler(), "1.0", taskDAOmock, memberDAOmock);
    private final SendMessageAnswer sendMessageAnswer = new SendMessageAnswer();
    private final Player playerMock = mock(Player.class);

    @BeforeEach
    void setUp() throws SQLException, DataAccessException {
        when(memberDAOmock.findMember(anyString())).thenReturn(MEMBER);
        when(memberDAOmock.findMember(anyInt())).thenReturn(MEMBER);
        when(memberDAOmock.findMember(any(UUID.class))).thenReturn(MEMBER);
        MEMBER.setId(MEMBER_ID);

        when(playerMock.getName()).thenReturn(PLAYER_NAME);
        when(playerMock.getUniqueId()).thenReturn(MEMBER_UUID);
        doAnswer(sendMessageAnswer).when(playerMock).sendMessage(anyString());
        doAnswer(sendMessageAnswer).when(playerMock).sendMessage(any(String[].class));
    }

    @Test
    void givenNoActiveTasks_noActiveTasksMessageIsSendToPlayer_whenNoSubcommandIsSpecified() throws SQLException, DataAccessException {
        when(taskDAOmock.fetchPlayersActiveTasks(MEMBER_ID)).thenReturn(List.of());

        tasks.onCommand(playerMock, FAKE_COMMAND, "", new String[]{});

        verify(memberDAOmock, times(1)).findMember(MEMBER_UUID);
        verify(taskDAOmock, times(1)).fetchPlayersActiveTasks(MEMBER_ID);
        assertThat(sendMessageAnswer.getMessages()).isEqualTo(List.of(ChatColor.GREEN + "JohnDoe has no tasks"));
    }

    @Test
    void givenExistingActiveTasks_activeTasksAreMessagedToPlayer_whenNoSubcommandIsSpecified() throws SQLException, DataAccessException {
        when(taskDAOmock.fetchPlayersActiveTasks(MEMBER_ID)).thenReturn(createActiveTasks());

        tasks.onCommand(playerMock, FAKE_COMMAND, "", new String[]{});

        verify(memberDAOmock, times(1)).findMember(MEMBER_UUID);
        verify(taskDAOmock, times(1)).fetchPlayersActiveTasks(MEMBER_ID);
        assertThat(sendMessageAnswer.getMessages())
                .isEqualTo(List.of(
                        "%s-=-=-=- %s's tasks -=-=-=-".formatted(ChatColor.AQUA, MEMBER_NAME),
                        "%s[3] %sFirst Task".formatted(ChatColor.GREEN, ChatColor.WHITE),
                        "%s[20] %sSecond Task".formatted(ChatColor.GOLD, ChatColor.WHITE)
                ));
    }

    @Test
    void givenNoPreparedTasks_noPreparedTasksMessageIsSendToPlayer_whenPreparedSubcommandIsSpecified() throws SQLException {
        when(taskDAOmock.fetchPreparedTasks()).thenReturn(List.of());

        tasks.onCommand(playerMock, FAKE_COMMAND, "", new String[]{"prepared"});

        verify(taskDAOmock, times(1)).fetchPreparedTasks();
        assertThat(sendMessageAnswer.getMessages()).isEqualTo(List.of(ChatColor.GREEN + "No prepared tasks"));
    }

    @Test
    void givenExistingPreparedTasks_preparedTasksAreMessagedToPlayer_whenPreparedSubcommandIsSpecified() throws SQLException {
        when(taskDAOmock.fetchPreparedTasks()).thenReturn(createPreparedTasks());

        tasks.onCommand(playerMock, FAKE_COMMAND, "", new String[]{"prepared"});

        verify(taskDAOmock, times(1)).fetchPreparedTasks();
        assertThat(sendMessageAnswer.getMessages())
                .isEqualTo(List.of(
                        "%s-=-=-=- Prepared tasks -=-=-=-".formatted(ChatColor.GRAY),
                        "%s[11] %sFirst Prepared Task".formatted(ChatColor.GRAY, ChatColor.WHITE),
                        "%s[15] %sSecond Prepared Task".formatted(ChatColor.GRAY, ChatColor.WHITE)
                ));
    }

    @Test
    void givenNoSupervisedTasks_noSupervisedTasksMessageIsSendToPlayer_whenSupervisedSubcommandIsSpecified() throws SQLException, DataAccessException {
        when(taskDAOmock.fetchAdvisorActiveTasks(MEMBER_ID)).thenReturn(List.of());

        tasks.onCommand(playerMock, FAKE_COMMAND, "", new String[]{"supervised"});

        verify(memberDAOmock, times(1)).findMember(MEMBER_UUID);
        verify(taskDAOmock, times(1)).fetchAdvisorActiveTasks(MEMBER_ID);
        assertThat(sendMessageAnswer.getMessages()).isEqualTo(List.of(ChatColor.GREEN + "No active supervised tasks"));
    }

    @Test
    void givenExistingSupervisedTasks_supervisedTasksAreMessagedToPlayer_whenSupervisedSubcommandIsSpecified() throws SQLException, DataAccessException {
        when(taskDAOmock.fetchAdvisorActiveTasks(MEMBER_ID)).thenReturn(createAdvisedTasks());

        tasks.onCommand(playerMock, FAKE_COMMAND, "", new String[]{"supervised"});

        verify(memberDAOmock, times(1)).findMember(MEMBER_UUID);
        verify(taskDAOmock, times(1)).fetchAdvisorActiveTasks(MEMBER_ID);
        assertThat(sendMessageAnswer.getMessages())
                .isEqualTo(List.of(
                        "%s-=-=-=- %s's supervised tasks -=-=-=-".formatted(ChatColor.LIGHT_PURPLE, PLAYER_NAME),
                        "%s[5] %sFirst Advised Task%s (JohnDoe)".formatted(ChatColor.GREEN, ChatColor.WHITE, ChatColor.ITALIC),
                        "%s[17] %sSecond Advised Task%s (JaneSmith)".formatted(ChatColor.GOLD, ChatColor.WHITE, ChatColor.ITALIC)
                ));
    }

    @Test
    void givenNoIdleTasks_noIdleTasksMessageIsSendToPlayer_whenIdleSubcommandIsSpecified() throws SQLException {
        when(taskDAOmock.fetchIdleTasks()).thenReturn(List.of());

        tasks.onCommand(playerMock, FAKE_COMMAND, "", new String[]{"idle"});

        verify(taskDAOmock, times(1)).fetchIdleTasks();
        assertThat(sendMessageAnswer.getMessages()).isEqualTo(List.of(ChatColor.GREEN + "No idle tasks"));
    }

    @Test
    void givenExistingIdleTasks_idleTasksAreMessagedToPlayer_whenIdleSubcommandIsSpecified() throws SQLException {
        when(taskDAOmock.fetchIdleTasks()).thenReturn(createIdleTasks());

        tasks.onCommand(playerMock, FAKE_COMMAND, "", new String[]{"idle"});

        verify(taskDAOmock, times(1)).fetchIdleTasks();
        assertThat(sendMessageAnswer.getMessages())
                .isEqualTo(List.of(
                        "%s-=-=-=- Idle tasks -=-=-=-".formatted(ChatColor.DARK_AQUA),
                        "%s[7] %sFirst Idle Task%s (10 days)".formatted(ChatColor.GOLD, ChatColor.WHITE, ChatColor.ITALIC),
                        "%s[13] %sSecond Idle Task%s (5 days)".formatted(ChatColor.GOLD, ChatColor.WHITE, ChatColor.ITALIC)
                ));
    }

    private List<Task> createActiveTasks() {
        return List.of(
                createTask(3, "First Task", TaskStatus.FINISHED),
                createTask(20, "Second Task", TaskStatus.DOING)
        );
    }

    private List<Task> createPreparedTasks() {
        return List.of(
                createTask(11, "First Prepared Task", TaskStatus.PREPARED),
                createTask(15, "Second Prepared Task", TaskStatus.PREPARED)
        );
    }

    private Task createTask(int id, String title, TaskStatus status) {
        var task = new Task(
                title,
                "",
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                status,
                new Date(0),
                new Date(0),
                new Date(0)
        );
        task.setId(id);
        return task;
    }

    private List<AdvisedTask> createAdvisedTasks() {
        return List.of(
                new AdvisedTask(5, "First Advised Task", null, TaskStatus.FINISHED, MEMBER_NAME),
                new AdvisedTask(17, "Second Advised Task", null, TaskStatus.DOING, MEMBER_NAME_2)
        );
    }

    private List<IdleTask> createIdleTasks() {
        long tenDaysAgo = LocalDate.now().minusDays(10).toEpochSecond(LocalTime.now(), ZoneOffset.UTC) * 1000;
        long fiveDaysAgo = LocalDate.now().minusDays(5).toEpochSecond(LocalTime.now(), ZoneOffset.UTC) * 1000;
        return List.of(
                new IdleTask(7, "First Idle Task", null, new Date(tenDaysAgo), null, null),
                new IdleTask(13, "Second Idle Task", null, new Date(fiveDaysAgo), null, null)
        );
    }
}
