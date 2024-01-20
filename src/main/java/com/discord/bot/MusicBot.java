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
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
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
            // Retrieve the member who sent the message
            Member member = event.getMember();
            if (member != null) {
                // Pass the member along with other parameters
                loadAndPlay((TextChannel) event.getChannel(), command[1], member, event.getGuild());
            } else {
                event.getChannel().sendMessage("You need to be in a voice channel to play music.").queue();
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
                channel.sendMessage("Added to queue " + track.getInfo().title).queue();

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

            // Подключаемся к голосовому каналу пользователя, если бот еще не подключен
            if (!audioManager.isConnected()) {
                Member member = guild.getMemberById(musicManager.requesterId); // Убедитесь, что requesterId определен в вашем классе GuildMusicManager
                if (member != null && member.getVoiceState() != null) {
                    AudioChannelUnion audioChannelUnion = member.getVoiceState().getChannel();
                    if (audioChannelUnion instanceof VoiceChannel) {
                        VoiceChannel voiceChannel = (VoiceChannel) audioChannelUnion;
                        audioManager.openAudioConnection(voiceChannel);
                    }
                }
            }
    
            musicManager.audioPlayer.playTrack(nextTrack);
        }
    }
}