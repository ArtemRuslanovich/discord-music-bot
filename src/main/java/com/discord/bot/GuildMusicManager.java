package com.discord.bot;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;

public class GuildMusicManager extends AudioEventAdapter {
    public final AudioPlayer audioPlayer;
    public final AudioQueue audioQueue;

    public GuildMusicManager(AudioPlayerManager playerManager, AudioQueue queue) {
        this.audioPlayer = playerManager.createPlayer();
        this.audioQueue = queue;
        this.audioPlayer.addListener(this);
    }
    
    public AudioPlayerSendHandler getSendHandler() {
        return new AudioPlayerSendHandler(audioPlayer);
    }
    public AudioQueue getAudioQueue() {
        return audioQueue;
    }
}