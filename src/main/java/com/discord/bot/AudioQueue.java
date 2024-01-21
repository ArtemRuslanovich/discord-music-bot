package com.discord.bot;

import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public class AudioQueue {
    private static final Logger logger = Logger.getLogger(AudioPlayerSendHandler.class.getName());
    private Queue<AudioTracks> queue;
    private AudioTracks nowPlaying;

    public AudioQueue() {
        this.queue = new LinkedList<>();
        this.nowPlaying = null;
    }

    public void addTrack(com.sedmelluq.discord.lavaplayer.track.AudioTrack track) {
        AudioTracks audioTracks = new AudioTracks(track.getInfo().title, track.getInfo().uri);
        audioTracks.setTrack(track); // Установка AudioTrack
        queue.add(audioTracks);
        logger.info(String.format("Track added to queue: {}", track.getInfo().title));
    }

    public AudioTrack poll() {
        AudioTracks audioTracks = queue.poll();
        if (audioTracks != null) {
            logger.info(String.format("Polling track from queue: {}", audioTracks.getTrack().getInfo().title));
            return audioTracks.getTrack();
        } else {
            logger.info("Queue is empty, no track to poll.");
        }
        return null;
    }
    

    public void clearQueue() {
        queue.clear();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public AudioTracks getNowPlaying() {
        return nowPlaying;
    }

    public void setNowPlaying(AudioTracks track) {
        nowPlaying = track;
    }
}