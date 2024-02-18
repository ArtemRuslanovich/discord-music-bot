package com.discord.bot;

import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public class AudioQueue {
    private static final Logger logger = Logger.getLogger(AudioQueue.class.getName());
    private Queue<QueueItem> queue; // Use QueueItem instead of AudioTrack
    private QueueItem nowPlaying;

    public AudioQueue() {
        this.queue = new LinkedList<>();
        this.nowPlaying = null;
    }

    public void addTrack(AudioTrack track, long requesterId) {
        if (track == null) {
            logger.warning("Attempted to add a null track to the queue.");
            return;
        }
        queue.add(new QueueItem(track, requesterId));
        logger.info(String.format("Track added to queue: %s by requester ID: %d", track.getInfo().title, requesterId));
    }

    public AudioTrack poll() {
        nowPlaying = queue.poll();
        if (nowPlaying != null) {
            logger.info(String.format("Polling track from queue: %s", nowPlaying.getTrack().getInfo().title));
            return nowPlaying.getTrack();
        } else {
            logger.info("Queue is empty, no track to poll.");
            return null;
        }
    }

    public void clearQueue() {
        queue.clear();
        nowPlaying = null; // Clear now playing when queue is cleared
        logger.info("Queue and now playing track cleared.");
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public AudioTrack getNowPlaying() {
        return nowPlaying != null ? nowPlaying.getTrack() : null;
    }

    // If needed, create a method to get the requester ID of the now playing track
    public long getNowPlayingRequesterId() {
        return nowPlaying != null ? nowPlaying.getRequesterId() : -1; // Return -1 or another invalid value if no track is playing
    }
}