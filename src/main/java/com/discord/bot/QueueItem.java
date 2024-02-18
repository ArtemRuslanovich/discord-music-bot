package com.discord.bot;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public class QueueItem {
    private final AudioTrack track;
    private final long requesterId; // Assuming ID is a long. Adjust type if necessary.

    public QueueItem(AudioTrack track, long requesterId) {
        this.track = track;
        this.requesterId = requesterId;
    }

    public AudioTrack getTrack() {
        return track;
    }

    public long getRequesterId() {
        return requesterId;
    }
}