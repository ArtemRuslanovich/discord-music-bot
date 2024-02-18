package com.discord.bot;

import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import net.dv8tion.jda.api.audio.AudioSendHandler;

public class AudioPlayerSendHandler implements AudioSendHandler {
    private static final Logger logger = Logger.getLogger(AudioPlayerSendHandler.class.getName());

    static {
        // Ensure the logger level is set to INFO so we can see the logs
        logger.setLevel(Level.INFO);
    }

    private final AudioPlayer audioPlayer;
    private AudioFrame lastFrame;

    public AudioPlayerSendHandler(AudioPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;
        logger.info("AudioPlayerSendHandler initialized.");
    }

    @Override
    public boolean canProvide() {
        lastFrame = lastFrame != null ? lastFrame : audioPlayer.provide();
        return lastFrame != null;
    }

    @Override
    public ByteBuffer provide20MsAudio() {
        if (lastFrame == null) {
            logger.warning("No audio frame is available to provide, despite canProvide() returning true.");
            return null;
        }

        ByteBuffer audioData = ByteBuffer.wrap(lastFrame.getData());
        if (!audioData.hasRemaining()) {
            logger.warning("The provided audio data is empty or invalid.");
            lastFrame = null; // Reset to fetch a new frame next time
            return null;
        }

        logger.info("Providing 20ms audio. Frame info: " + lastFrame.toString());
        lastFrame = null; // Reset to fetch a new frame next time
        return audioData;
    }

    @Override
    public boolean isOpus() {
        return true;
    }
}