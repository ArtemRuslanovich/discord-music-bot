package com.discord.bot;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
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
import java.util.Map;
import java.util.logging.Logger;

public class MusicBot extends ListenerAdapter {
    private static final Logger logger = Logger.getLogger(MusicBot.class.getName());
    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers;

    public MusicBot() {
        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
        this.musicManagers = new HashMap<>();
        logger.info("MusicBot initialized");
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        String[] command = event.getMessage().getContentRaw().split(" ", 2);

        if (command[0].equalsIgnoreCase("!play") && command.length > 1) {
            Member member = event.getMember();
            if (member != null) {
                try {
                    loadAndPlay(command[1], member, event);
                } catch (ClassCastException e) {
                    logger.warning("Command not in a TextChannel: " + command[1]);
                }
            } else {
                event.getChannel().sendMessage("Error: Member not found.").queue();
                logger.warning("Member not found for command: " + command[1]);
            }
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

    private void loadAndPlay(final String trackUrl, final Member member, final MessageReceivedEvent event) {
        Guild guild = event.getGuild();
        GuildMusicManager musicManager = getGuildAudioPlayer(guild);

        logger.info("Loading item: " + trackUrl);
        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                try {
                    TextChannel channel = (TextChannel) event.getChannel();
                    channel.sendMessage("Adding to queue " + track.getInfo().title).queue();
                    musicManager.getAudioQueue().addTrack(track);
                    play(guild, musicManager, member);
                } catch (ClassCastException e) {
                    logger.warning("Track loaded but channel is not a TextChannel: " + trackUrl);
                }
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                // Implement playlist handling logic here
            }

            @Override
            public void noMatches() {
                try {
                    TextChannel channel = (TextChannel) event.getChannel();
                    channel.sendMessage("No matches found for " + trackUrl).queue();
                } catch (ClassCastException e) {
                    logger.warning("No matches and channel is not a TextChannel: " + trackUrl);
                }
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                try {
                    TextChannel channel = (TextChannel) event.getChannel();
                    channel.sendMessage("Could not play: " + exception.getMessage()).queue();
                } catch (ClassCastException e) {
                    logger.warning("Load failed and channel is not a TextChannel: " + trackUrl);
                }
            }
        });
    }

    private void play(Guild guild, GuildMusicManager musicManager, Member commandIssuer) {
        if (musicManager.getAudioQueue().isEmpty()) {
            logger.info("Audio queue is empty.");
            return;
        }
    
        connectToMemberVoiceChannel(guild, musicManager, commandIssuer);
    
        AudioTrack nextTrack = musicManager.getAudioQueue().poll();
        musicManager.audioPlayer.playTrack(nextTrack);
        logger.info("Playing track: " + nextTrack.getInfo().title);
    }
    
    private void connectToMemberVoiceChannel(Guild guild, GuildMusicManager musicManager, Member commandIssuer) {
        AudioManager audioManager = guild.getAudioManager();
        if (audioManager.isConnected()) {
            logger.info("Already connected to a voice channel.");
            return;
        }
    
        GuildVoiceState memberVoiceState = commandIssuer.getVoiceState();
        if (memberVoiceState != null && memberVoiceState.getChannel() != null) {
            VoiceChannel memberChannel = (VoiceChannel) memberVoiceState.getChannel();
            if (memberChannel != null) {
                try {
                    audioManager.openAudioConnection(memberChannel);
                    logger.info("Connected to voice channel: " + memberChannel.getName());
                } catch (Exception e) {
                    logger.severe("Failed to connect to voice channel: " + e.getMessage());
                }
            } else {
                logger.warning("Command issuer is not in a valid voice channel.");
            }
        } else {
            logger.warning("Command issuer is not in a voice channel.");
        }
    }
}
