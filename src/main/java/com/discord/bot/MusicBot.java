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
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.entities.Member;

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

        String[] command = event.getMessage().getContentRaw().split(" ");

        if (command[0].equalsIgnoreCase("!play") && command.length > 1) {
            Member member = event.getMember();
            if (member != null) {
                loadAndPlay((TextChannel) event.getChannel(), command[1], member, event.getGuild());
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
            logger.info("Created new GuildMusicManager for guild: " + guild.getName());
        }

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }

    private void loadAndPlay(final TextChannel channel, final String trackUrl, final Member member, Guild guild) {
        GuildMusicManager musicManager = getGuildAudioPlayer(guild);

        logger.info("Loading item: " + trackUrl);
        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                channel.sendMessage("Adding to queue " + track.getInfo().title).queue();
                logger.info("Track loaded and added to queue: " + track.getInfo().title);

                musicManager.setRequesterId(member.getIdLong());
                musicManager.getAudioQueue().addTrack(track);

                play(guild, musicManager);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                // Обработка плейлиста
            }

            @Override
            public void noMatches() {
                channel.sendMessage("No matches found for " + trackUrl).queue();
                logger.warning("No matches found for URL: " + trackUrl);
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                channel.sendMessage("Could not play: " + exception.getMessage()).queue();
                logger.severe("Error loading track: " + exception.getMessage());
            }
        });
    }

    private void play(Guild guild, GuildMusicManager musicManager) {
        if (!musicManager.getAudioQueue().isEmpty()) {
            AudioTrack nextTrack = musicManager.getAudioQueue().poll();
            AudioManager audioManager = guild.getAudioManager();

            if (!audioManager.isConnected()) {
                Member member = guild.getMemberById(musicManager.getRequesterId());
                if (member != null) {
                    GuildVoiceState voiceState = member.getVoiceState();
                    if (voiceState != null && voiceState.getChannel() != null) {
                        AudioChannel channel = voiceState.getChannel();
                        if (channel instanceof VoiceChannel) {
                            VoiceChannel voiceChannel = (VoiceChannel) channel;
                            try {
                                audioManager.openAudioConnection(voiceChannel);
                                logger.info("Connected to voice channel: " + voiceChannel.getName());
                            } catch (Exception e) {
                                logger.severe("Failed to connect to voice channel: " + e.getMessage());
                                return;
                            }
                        } else {
                            logger.warning("The channel is not a voice channel.");
                            return;
                        }
                    } else {
                        logger.warning("Member is not in a voice channel or voice state is null.");
                        return;
                    }
                } else {
                    logger.warning("Member with ID " + musicManager.getRequesterId() + " not found.");
                    return;
                }
            }

            try {
                musicManager.audioPlayer.playTrack(nextTrack);
                logger.info("Playing track: " + nextTrack.getInfo().title);
            } catch (Exception e) {
                logger.severe("Error playing track: " + e.getMessage());
            }
        } else {
            logger.info("Audio queue is empty.");
        }
    }
    // Остальные методы...
}
