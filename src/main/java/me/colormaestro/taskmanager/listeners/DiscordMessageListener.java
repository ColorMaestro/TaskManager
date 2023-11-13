package me.colormaestro.taskmanager.listeners;

import me.colormaestro.taskmanager.integrations.DiscordOperator;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class DiscordMessageListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.isFromType(ChannelType.PRIVATE)) {
            return;
        }

        String content = event.getMessage().getContentRaw();
        if (!content.startsWith("!code")) {
            return;
        }

        String[] array = content.split(" ");
        String code;

        try {
            code = array[1];
        } catch (ArrayIndexOutOfBoundsException ex) {
            event.getChannel().sendMessage(":x: You must specify code").queue();
            return;
        }

        boolean result = DiscordOperator.getInstance().verifyCode(code, event.getAuthor().getIdLong());
        if (result) {
            event.getChannel().sendMessage(":ballot_box_with_check: Authentication done").queue();
        } else {
            event.getChannel().sendMessage(":x: Invalid code").queue();
        }
    }
}
