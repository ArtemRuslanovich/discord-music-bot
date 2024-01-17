package com.discord.bot;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MusicBot extends ListenerAdapter {
    private AudioQueue audioQueue;

    public MusicBot() {
        this.audioQueue = new AudioQueue();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String[] command = event.getMessage().getContentRaw().split(" ");

        if (command[0].equalsIgnoreCase("!play")) {
            // Пример: !play <название трека> <URL>
            String trackTitle = command[1];
            String trackUrl = command[2];
            AudioTrack track = new AudioTrack(trackTitle, trackUrl);
            audioQueue.addTrack(track);
            event.getChannel().sendMessage("Трек добавлен в очередь: " + track.getTitle()).queue();
        } else if (command[0].equalsIgnoreCase("!skip")) {
            // Пример: !skip
            if (!audioQueue.isEmpty()) {
                AudioTrack currentTrack = audioQueue.removeTrack();
                event.getChannel().sendMessage("Пропущен трек: " + currentTrack.getTitle()).queue();
            } else {
                event.getChannel().sendMessage("Очередь воспроизведения пуста.").queue();
            }
        } else if (command[0].equalsIgnoreCase("!clear")) {
            // Пример: !clear
            audioQueue.clearQueue();
            event.getChannel().sendMessage("Очередь воспроизведения очищена.").queue();
        }
    }
}