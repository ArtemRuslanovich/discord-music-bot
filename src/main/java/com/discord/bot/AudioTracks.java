package com.discord.bot;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public class AudioTracks {
    private String title;
    private String url;
    private AudioTrack track;

    public AudioTracks(String title, String url) {
        this.title = title;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public AudioTrack getTrack() {
        return track;
    }

    public void setTrack(AudioTrack track) {
        this.track = track;
    }
}