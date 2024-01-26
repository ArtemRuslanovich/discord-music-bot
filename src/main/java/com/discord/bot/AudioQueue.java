package com.discord.bot;

import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public class AudioQueue {
    private static final Logger logger = Logger.getLogger(AudioQueue.class.getName());
    private Queue<AudioTrack> queue;  // Assuming AudioTrack is the correct type
    private AudioTrack nowPlaying;

    public AudioQueue() {
        this.queue = new LinkedList<>();
        this.nowPlaying = null;
    }

    public void addTrack(AudioTrack track) {
        queue.add(track);
        logger.info(String.format("Track added to queue: %s", track.getInfo().title));
    }

    public AudioTrack poll() {
        AudioTrack track = queue.poll();
        if (track != null) {
            logger.info(String.format("Polling track from queue: %s", track.getInfo().title));
            return track;
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

    public AudioTrack getNowPlaying() {
        return nowPlaying;
    }

    public void setNowPlaying(AudioTrack track) {
        nowPlaying = track;
    }
}