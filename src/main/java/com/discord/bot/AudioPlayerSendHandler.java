package com.discord.bot;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import net.dv8tion.jda.api.audio.AudioSendHandler;

import java.nio.ByteBuffer;

public class AudioPlayerSendHandler implements AudioSendHandler {
    private final AudioPlayer audioPlayer;
    private final ByteBuffer buffer;
    private final MutableAudioFrame frame;

    public AudioPlayerSendHandler(AudioPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;
        // Allocate a ByteBuffer for Discord's maximum size of 2048 bytes
        this.buffer = ByteBuffer.allocate(2048);
        // Create a MutableAudioFrame for reuse in canProvide()
        this.frame = new MutableAudioFrame();
        this.frame.setBuffer(buffer);
    }

    @Override
    public boolean canProvide() {
        // Delegate to audioPlayer to provide data for our frame
        return audioPlayer.provide(frame);
    }

    @Override
    public ByteBuffer provide20MsAudio() {
        // Flip the buffer to make it ready for reading
        buffer.flip();
        return buffer;
    }

    @Override
    public boolean isOpus() {
        // Indicate that we are providing audio in opus format
        return true;
    }
}