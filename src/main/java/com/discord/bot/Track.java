package com.discord.bot;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

/**
 * Represents information about a single audio track.
 */
public class Track {
    private AudioTrack track;

    public Track(AudioTrack track) {
        this.track = track;
    }

    public String getTitle() {
        return track.getInfo().title;
    }

    public String getUrl() {
        return track.getInfo().uri;
    }

    public AudioTrack getTrack() {
        return track;
    }

    public void setTrack(AudioTrack track) {
        if (track == null) {
            throw new IllegalArgumentException("AudioTrack cannot be null");
        }
        this.track = track;
    }
}
