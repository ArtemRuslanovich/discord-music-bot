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
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.entities.Member;

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

        if (command[0].equalsIgnoreCase("!play") && command.length > 1) {
            Member member = event.getMember();
            if (member != null) {
                loadAndPlay((TextChannel) event.getChannel(), command[1], member, event.getGuild());
            } else {
                event.getChannel().sendMessage("Error: Member not found.").queue();
            }
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

    private void loadAndPlay(final TextChannel channel, final String trackUrl, final Member member, Guild guild) {
        GuildMusicManager musicManager = getGuildAudioPlayer(guild);
    
        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                channel.sendMessage("Adding to queue " + track.getInfo().title).queue();
    
                // Set the requesterId when a track is loaded
                musicManager.setRequesterId(member.getIdLong());
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
                                System.out.println("Trying to connect to voice channel: " + voiceChannel.getName());
                            } catch (Exception e) {
                                e.printStackTrace();
                                System.out.println("Failed to connect to voice channel: " + e.getMessage());
                                return; // Если подключение не удалось, выходим из метода
                            }
                        } else {
                            System.out.println("The channel is not a voice channel.");
                            return; // Если канал не является голосовым, выходим из метода
                        }
                    } else {
                        System.out.println("Member is not in a voice channel or voice state is null.");
                        return; // Если голосовой статус не найден, выходим из метода
                    }
                } else {
                    System.out.println("Member with ID " + musicManager.getRequesterId() + " not found.");
                    return; // Если участник не найден, выходим из метода
                }
            }
    
            try {
                musicManager.audioPlayer.playTrack(nextTrack);
                System.out.println("Playing track: " + nextTrack.getInfo().title);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error playing track: " + e.getMessage());
            }
        } else {
            System.out.println("Audio queue is empty.");
        }
    }
}