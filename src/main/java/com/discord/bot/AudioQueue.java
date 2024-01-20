package com.discord.bot;

import java.util.LinkedList;
import java.util.Queue;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public class AudioQueue {
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
    }

    public AudioTrack poll() {
        AudioTracks audioTracks = queue.poll();
        if (audioTracks != null) {
            return audioTracks.getTrack();
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