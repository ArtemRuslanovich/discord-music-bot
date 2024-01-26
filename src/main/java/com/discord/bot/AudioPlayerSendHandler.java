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
        // Fetch a new frame if the last one has been consumed
        if (lastFrame == null) {
            lastFrame = audioPlayer.provide();
        }

        // Log whether a frame is available
        logger.fine("Can provide: " + (lastFrame != null));
    
        // Return true if a frame is available, false otherwise
        return lastFrame != null;
    }

    @Override
    public ByteBuffer provide20MsAudio() {
        // Return null if there is no frame available to provide
        if (lastFrame == null) {
            logger.fine("No audio frame available to provide");
            return null;
        }

        // Wrap the audio data in a ByteBuffer
        ByteBuffer audioData = ByteBuffer.wrap(lastFrame.getData());
        logger.fine("Providing 20ms audio");

        // Reset lastFrame to ensure the next frame is fetched
        lastFrame = null;

        return audioData;
    }

    @Override
    public boolean isOpus() {
        // Lavaplayer already encodes data in Opus format
        return true;
    }
}