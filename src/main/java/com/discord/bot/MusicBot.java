package com.discord.bot;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.HashMap;
import java.util.Map;

public class MusicBot extends ListenerAdapter {
    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers;

    public MusicBot() {
        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
        this.musicManagers = new HashMap<>();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        String[] command = event.getMessage().getContentRaw().split(" ");

        if (command[0].equalsIgnoreCase("!play")) {
            loadAndPlay((TextChannel) event.getChannel(), command[1], event.getGuild());
        }
    }

    private synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        GuildMusicManager musicManager = musicManagers.get(guildId);

        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager, new AudioQueue());
            musicManagers.put(guildId, musicManager);
        }

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }

    private void loadAndPlay(final TextChannel channel, final String trackUrl, Guild guild) {
        GuildMusicManager musicManager = getGuildAudioPlayer(guild);
    
        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                channel.sendMessage("Added to queue " + track.getInfo().title).queue();
                musicManager.getAudioQueue().addTrack(track);
                play(guild, musicManager);
            }
    
            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                // handle playlist
            }
    
            @Override
            public void noMatches() {
                channel.sendMessage("No matches found for " + trackUrl).queue();
            }
    
            @Override
            public void loadFailed(FriendlyException exception) {
                channel.sendMessage("Could not play: " + exception.getMessage()).queue();
            }
        });
    }

    private void play(Guild guild, GuildMusicManager musicManager) {
        if (!musicManager.getAudioQueue().isEmpty() && musicManager.audioPlayer.getPlayingTrack() == null) {
            AudioTrack nextTrack = musicManager.getAudioQueue().poll();
            musicManager.audioPlayer.playTrack(nextTrack);
        }
    }
}