package me.colormaestro.taskmanager.commands;

import me.colormaestro.taskmanager.FakeCommand;
import me.colormaestro.taskmanager.SendMessageAnswer;
import me.colormaestro.taskmanager.data.DataAccessException;
import me.colormaestro.taskmanager.data.MemberDAO;
import me.colormaestro.taskmanager.data.TaskDAO;
import me.colormaestro.taskmanager.enums.TaskStatus;
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
    private static final String MEMBER_NAME = "JohnDoe";
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

    private List<Task> createActiveTasks() {
        return List.of(
                createTask(3, "First Task", TaskStatus.FINISHED),
                createTask(20, "Second Task", TaskStatus.DOING)
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
}
