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
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

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
            logger.info("Ready to play, but audio queue is empty.");
            return;
        }

        // Assuming you've adjusted to pass the Member who requested the track
        Member commandIssuer = guild.getMemberById(musicManager.getRequesterId());
        if (commandIssuer == null) {
            logger.warning("Requester of the track is not found in the guild.");
            return;
        }

        if (!connectToMemberVoiceChannel(guild, musicManager, commandIssuer)) {
            logger.warning("Unable to connect to the member's voice channel.");
            return;
        }

        AudioTrack nextTrack = musicManager.getAudioQueue().poll();
        if (nextTrack != null) {
            musicManager.audioPlayer.playTrack(nextTrack);
            logger.info("Playing track: " + nextTrack.getInfo().title);
        } else {
            logger.severe("Track was expected to play but was null after polling.");
        }
    }

    private boolean connectToMemberVoiceChannel(Guild guild, GuildMusicManager musicManager, Member commandIssuer) {
        AudioManager audioManager = guild.getAudioManager();
        if (audioManager.isConnected()) {
            logger.info("Already connected to a voice channel or attempt in progress.");
            return true;
        }

        GuildVoiceState memberVoiceState = commandIssuer.getVoiceState();
        if (memberVoiceState != null && memberVoiceState.inAudioChannel()) {
            audioManager.openAudioConnection(memberVoiceState.getChannel());
            logger.info("Connected to voice channel: " + memberVoiceState.getChannel().getName());
            return true;
        } else {
            logger.warning("Command issuer is not in a voice channel or voice state is null.");
            return false;
        }
    }

}
