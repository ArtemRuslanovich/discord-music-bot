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
            // !play <название трека> <URL>
            String trackTitle = command[1];
            String trackUrl = command[2];
            AudioTrack track = new AudioTrack(trackTitle, trackUrl);
            audioQueue.addTrack(track);
            audioQueue.setNowPlaying(track); // Установка текущего трека
            event.getChannel().sendMessage("Трек добавлен в очередь: " + track.getTitle()).queue();
        } else if (command[0].equalsIgnoreCase("!skip")) {
            // !skip
            if (!audioQueue.isEmpty()) {
                AudioTrack currentTrack = audioQueue.removeTrack();
                audioQueue.setNowPlaying(currentTrack); // Установка текущего трека после пропуска
                event.getChannel().sendMessage("Пропущен трек: " + currentTrack.getTitle()).queue();
            } else {
                event.getChannel().sendMessage("Очередь воспроизведения пуста.").queue();
            }
        } else if (command[0].equalsIgnoreCase("!clear")) {
            // !clear
            audioQueue.clearQueue();
            audioQueue.setNowPlaying(null); // Очистка текущего трека
            event.getChannel().sendMessage("Очередь воспроизведения очищена.").queue();
        } else if (command[0].equalsIgnoreCase("!nowplaying")) {
            // !nowplaying
            AudioTrack nowPlaying = audioQueue.getNowPlaying();
            if (nowPlaying != null) {
                event.getChannel().sendMessage("Сейчас играет: " + nowPlaying.getTitle()).queue();
            } else {
                event.getChannel().sendMessage("В данный момент ничего не играет.").queue();
            }
        }
    }
}