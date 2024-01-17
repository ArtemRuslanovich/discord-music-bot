package com.discord.bot;

import java.util.LinkedList;
import java.util.Queue;

public class AudioQueue {
    private Queue<AudioTrack> queue;

    public AudioQueue() {
        this.queue = new LinkedList<>();
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
}