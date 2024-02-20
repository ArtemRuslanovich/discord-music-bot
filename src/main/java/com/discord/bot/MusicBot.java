package com.discord.bot;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.HashMap;
import java.util.List;
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
            logger.fine("Ignored message received from bot.");
            return;
        }

        String[] command = event.getMessage().getContentRaw().split(" ", 2);
        logger.fine("Received message: " + String.join(" ", command));

        if (command[0].equalsIgnoreCase("!play") && command.length > 1) {
            loadAndPlay(event, command[1]);
        }
    }

    private synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        GuildMusicManager musicManager = musicManagers.get(guildId);
        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager, new AudioQueue());
            musicManagers.put(guildId, musicManager);
            guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());
            logger.info("Created new GuildMusicManager for guild: " + guild.getName());
        }
        return musicManager;
    }

    public void loadAndPlay(final MessageReceivedEvent event, String trackUrl) {
        TextChannel channel = (TextChannel) event.getChannel();
        Guild guild = event.getGuild();
        Member member = event.getMember();
        GuildMusicManager musicManager = getGuildAudioPlayer(guild);
        logger.info("Loading item: " + trackUrl);

        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                long requesterId = member.getIdLong();
                logger.info("Track loaded successfully: " + track.getInfo().title);
                channel.sendMessage("Adding to queue " + track.getInfo().title).queue();
                musicManager.getAudioQueue().addTrack(track, requesterId);
                play(guild, musicManager);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                long requesterId = member.getIdLong();
                AudioTrack firstTrack = playlist.getSelectedTrack() != null ? playlist.getSelectedTrack() : playlist.getTracks().get(0);
                logger.info("Playlist loaded, adding first track to queue: " + firstTrack.getInfo().title);
                channel.sendMessage("Adding to queue " + firstTrack.getInfo().title + " (first track of playlist)").queue();
                musicManager.getAudioQueue().addTrack(firstTrack, requesterId);
                play(guild, musicManager);
            }

            @Override
            public void noMatches() {
                logger.warning("No matches found for: " + trackUrl);
                channel.sendMessage("No matches found for " + trackUrl).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                logger.log(Level.SEVERE, "Could not load track: " + trackUrl, exception);
                channel.sendMessage("Could not play: " + exception.getMessage()).queue();
            }
        });
    }

    private void play(Guild guild, GuildMusicManager musicManager) {
        if (musicManager.getAudioQueue().isEmpty()) {
            logger.info("Audio queue is empty, nothing to play.");
            return;
        }

        Member commandIssuer = guild.getMemberById(musicManager.getRequesterId());
        if (commandIssuer == null) {
            logger.info("The requester of the track is not found in the guild.");
            // Here, decide whether to stop operation or proceed without the requester.
            // For now, we choose to proceed, attempting to connect to a default or active channel.
        }

        // This part ensures the bot connects to a voice channel, whether it's the requester's channel or a default/active one.
        if (!connectToVoiceChannel(guild, commandIssuer)) {
            logger.warning("Unable to connect to a voice channel.");
            return;
        }

        // Now, play the next track.
        AudioTrack nextTrack = musicManager.getAudioQueue().poll();
        if (nextTrack != null) {
            musicManager.audioPlayer.playTrack(nextTrack);
            logger.info("Playing: " + nextTrack.getInfo().title);
        } else {
            logger.warning("Expected to play a track, but the next track was null.");
        }
    }

    private boolean connectToVoiceChannel(Guild guild, Member commandIssuer) {
        AudioManager audioManager = guild.getAudioManager();

        if (audioManager.isConnected()) {
            return true; // Already connected or in the process of connecting.
        }

        VoiceChannel channelToJoin = null;
        if (commandIssuer != null) {
            // Attempt to join the requester's channel.
            GuildVoiceState voiceState = commandIssuer.getVoiceState();
            if (voiceState != null && voiceState.inAudioChannel()) {
                channelToJoin = (VoiceChannel) commandIssuer.getVoiceState().getChannel();
            }
        }

        if (channelToJoin == null) {
            // Fallback: Join the first available voice channel or a specified default.
            // This part needs customization based on your bot's design or server setup.
            List<VoiceChannel> channels = guild.getVoiceChannels();
            if (!channels.isEmpty()) {
                channelToJoin = channels.get(0); // Simplistic approach: join the first channel.
            }
        }

        if (channelToJoin != null) {
            audioManager.openAudioConnection(channelToJoin);
            logger.info("Connected to voice channel: " + channelToJoin.getName());
            return true;
        } else {
            logger.info("No suitable voice channel found to join.");
            return false;
        }
    }
}
