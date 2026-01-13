package me.colormaestro.taskmanager;

import org.bukkit.entity.Player;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Lightweight construct for capturing of passed arguments to {@link Player#sendMessage(String)} and
 * {@link Player#sendMessage(String...)} methods. Since {@link Player} is very fat interface, this way one can avoid
 * creating its monstrous implementation.
 */
public class SendMessageAnswer implements Answer<Void> {
    private final List<String> messages = new ArrayList<>();

    @Override
    public Void answer(InvocationOnMock inv) {
        Object arg = inv.getArgument(0);
        if (arg instanceof String) {
            messages.add((String) arg);
        } else if (arg instanceof String[]) {
            messages.addAll(Arrays.asList((String[]) arg));
        }
        return null;
    }

    public List<String> getMessages() {
        return messages;
    }
}
