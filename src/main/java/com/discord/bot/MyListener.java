package com.discord.bot;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MyListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return; // Игнорируем сообщения от ботов

        String message = event.getMessage().getContentRaw();

        if (message.equalsIgnoreCase("!hello")) {
            event.getChannel().sendMessage("Hello, " + event.getAuthor().getName() + "!").queue();
        }
    }
}