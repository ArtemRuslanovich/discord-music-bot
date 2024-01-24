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
        if (lastFrame == null) {
            lastFrame = audioPlayer.provide();
        }
        boolean canProvide = lastFrame != null;
        logger.fine("Can provide: " + canProvide);

    return canProvide;
    }
    

    @Override
    public ByteBuffer provide20MsAudio() {
        ByteBuffer audioData = lastFrame != null ? ByteBuffer.wrap(lastFrame.getData()) : null;
        logger.fine("Providing 20ms audio"); // Changed to fine for less verbose logging
        return audioData;
    }

    @Override
    public boolean isOpus() {
        return true;
    }
}