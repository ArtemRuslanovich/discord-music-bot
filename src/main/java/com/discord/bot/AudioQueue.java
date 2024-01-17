package com.discord.bot;

import java.util.LinkedList;
import java.util.Queue;

public class AudioQueue {
    private Queue<AudioTrack> queue;
    private AudioTrack nowPlaying;

    public AudioQueue() {
        this.queue = new LinkedList<>();
        this.nowPlaying = null;
    }

    public void addTrack(AudioTrack track) {
        queue.add(track);
    }

    public AudioTrack removeTrack() {
        return queue.poll();
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