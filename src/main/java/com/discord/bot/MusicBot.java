package com.discord.bot;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MusicBot extends ListenerAdapter {
    private static final Logger logger = Logger.getLogger(MusicBot.class.getName());
    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers;

    static {
        logger.setLevel(Level.INFO);
    }

    public MusicBot() {
        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
        this.musicManagers = new HashMap<>();
        logger.info("MusicBot initialized.");
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return; // Ignore bot messages entirely
        }

        String[] command = event.getMessage().getContentRaw().split("\\s+", 2);
        if (command[0].equalsIgnoreCase("!play") && command.length > 1) {
            loadAndPlay(event, command[1]);
        }
        // Additional commands can be handled here
    }

    private synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        return musicManagers.computeIfAbsent(guild.getIdLong(), guildId -> {
            GuildMusicManager manager = new GuildMusicManager(playerManager, new AudioQueue());
            guild.getAudioManager().setSendingHandler(manager.getSendHandler());
            logger.info("Created new GuildMusicManager for guild: " + guild.getName());
            return manager;
        });
    }

    public void loadAndPlay(final MessageReceivedEvent event, String trackUrl) {
        TextChannel channel = (TextChannel) event.getChannel();
        Guild guild = event.getGuild();
        GuildMusicManager musicManager = getGuildAudioPlayer(guild);
        Member member = event.getMember();

        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                long requesterId = member.getIdLong();
                logger.info("Track loaded: " + track.getInfo().title);
                channel.sendMessage("Adding to queue " + track.getInfo().title).queue();
                musicManager.getAudioQueue().addTrack(track, requesterId); // Assuming your AudioQueue has an addTrack method
                play(guild, musicManager);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getSelectedTrack() != null ? playlist.getSelectedTrack() : playlist.getTracks().get(0);
                logger.info("Playlist loaded: " + firstTrack.getInfo().title);
                long requesterId = member.getIdLong();
                channel.sendMessage("Adding to queue " + firstTrack.getInfo().title + " (first track of playlist)").queue();
                musicManager.getAudioQueue().addTrack(firstTrack, requesterId);
                play(guild, musicManager);
            }

            @Override
            public void noMatches() {
                channel.sendMessage("No matches found for " + trackUrl).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                channel.sendMessage("Could not load track: " + exception.getMessage()).queue();
            }
        });
    }

    private void play(Guild guild, GuildMusicManager musicManager) {
        if (musicManager.getAudioQueue().isEmpty()) {
            logger.info("Queue is empty, nothing to play.");
            return;
        }

        guild.getAudioManager().openAudioConnection(findVoiceChannel(guild)); // Simplify your voice channel connection logic as needed
        AudioTrack nextTrack = musicManager.getAudioQueue().poll(); // Assuming your AudioQueue has a pollTrack method
        if (nextTrack != null) {
            musicManager.audioPlayer.playTrack(nextTrack);
            logger.info("Playing: " + nextTrack.getInfo().title);
        }
    }

    // This is a placeholder method. Implement your logic to find an appropriate voice channel.
    private VoiceChannel findVoiceChannel(Guild guild) {
        // Your logic to select a voice channel
        return guild.getVoiceChannels().get(0); // Simplistic approach: just select the first channel
    }
}