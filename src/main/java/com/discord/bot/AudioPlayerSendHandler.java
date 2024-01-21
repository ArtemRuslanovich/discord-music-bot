package com.discord.bot;

import java.nio.ByteBuffer;

import java.util.logging.Logger;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;

import net.dv8tion.jda.api.audio.AudioSendHandler;

public class AudioPlayerSendHandler implements AudioSendHandler {
    private static final Logger logger = Logger.getLogger(AudioPlayerSendHandler.class.getName());

    private final AudioPlayer audioPlayer;
    private AudioFrame lastFrame;

    public AudioPlayerSendHandler(AudioPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;
    }
    
    @Override
    public boolean canProvide() {
        lastFrame = audioPlayer.provide();
        boolean canProvide = lastFrame != null && lastFrame.getData().length > 0;
        logger.info(String.format("Can provide: ", canProvide));

        return canProvide;
    }

    @Override
    public ByteBuffer provide20MsAudio() {
        logger.info("Providing 20ms audio");
        return ByteBuffer.wrap(lastFrame.getData());
    }

    @Override
    public boolean isOpus() {
        return true;
    }
}