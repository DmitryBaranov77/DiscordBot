package dimon.bot.listeners;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.*;
import dimon.bot.listeners.music.GuildMusicManager;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.audio.SpeakingMode;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.springframework.stereotype.Service;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

@Service
public class MusicListener extends ListenerAdapter implements EventListener{

    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers;

    private MusicListener() {
        this.musicManagers = new HashMap<>();

        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    private synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        GuildMusicManager musicManager = musicManagers.get(guildId);

        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager);
            musicManagers.put(guildId, musicManager);
        }

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }

    @SneakyThrows
    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        String[] command = event.getMessage().getContentRaw().split(" ", 2);
        switch (command[0]) {
            case "~skip":
                skipTrack(event.getChannel());
                break;
            case "~leave":
                playByeMusic(event.getChannel(), "src/main/resources/sounds/kto-kuda-a-ya-po-delam.mp3");
                disconnectFromVoiceChannel(event.getGuild().getAudioManager());
                break;
            case "~join":
                connectToFirstVoiceChannel(event.getChannel().getGuild().getAudioManager());
                break;
        }
        if ("~play".equals(command[0]) && command.length == 2) {
            loadAndPlay(event.getChannel(), command[1]);
        }

        super.onGuildMessageReceived(event);
    }

    private void loadAndPlay(final TextChannel channel, final String trackUrl) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());

        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                channel.sendMessage("Adding to queue " + track.getInfo().title).queue();

                play(channel.getGuild(), musicManager, track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getSelectedTrack();

                if (firstTrack == null) {
                    firstTrack = playlist.getTracks().get(0);
                }

                channel.sendMessage("Adding to queue " + firstTrack.getInfo().title + " (first track of playlist " + playlist.getName() + ")").queue();

                play(channel.getGuild(), musicManager, firstTrack);
            }

            @Override
            public void noMatches() {
                channel.sendMessage("Nothing found by " + trackUrl).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                channel.sendMessage("Could not play: " + exception.getMessage()).queue();
            }
        });
    }

    private void play(Guild guild, GuildMusicManager musicManager, AudioTrack track) {
        connectToFirstVoiceChannel(guild.getAudioManager());
        musicManager.scheduler.queue(track);
    }

    private void skipTrack(TextChannel channel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        musicManager.scheduler.nextTrack();

        channel.sendMessage("Skipped to next track.").queue();
    }

    private static void connectToFirstVoiceChannel(AudioManager audioManager) {
        if (!audioManager.isConnected()) {
            audioManager.openAudioConnection(getFirstVoiceChannel(audioManager));
            audioManager.setSpeakingMode(SpeakingMode.SOUNDSHARE);
            audioManager.setSelfDeafened(true);
        }
    }

    private static VoiceChannel getFirstVoiceChannel(AudioManager audioManager) {
        return audioManager.getGuild().getVoiceChannels().get(0);
    }

    @SneakyThrows
    private static void disconnectFromVoiceChannel(AudioManager audioManager) {
        if (audioManager.isConnected()) {
            Thread.sleep(6000);
            audioManager.closeAudioConnection();
        }
    }

    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        TextChannel channel = event.getChannelJoined().getGuild().getDefaultChannel();
        if(event.getMember().getVoiceState().getChannel() == getFirstVoiceChannel(event.getGuild().getAudioManager())){
            if (!event.getMember().getUser().isBot()) {
                playHelloMusic(channel, "src/main/resources/sounds/О Привет мем.mp3");
            } else {
                playHelloMusic(channel, "src/main/resources/sounds/shizofreniya.mp3");
                playHelloMusic(channel, "src/main/resources/sounds/-blin-zachem-ya-syuda-prishel.mp3");
                playHelloMusic(channel, "src/main/resources/sounds/povezlo-povezlo.mp3");
            }
        }
    }

    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        VoiceChannel channelLeft = event.getChannelLeft();
        if(channelLeft == getFirstVoiceChannel(event.getGuild().getAudioManager()) && channelLeft.getMembers().size() == 1){
            getGuildAudioPlayer(event.getGuild()).scheduler.clearQueue();
            event.getGuild().getAudioManager().closeAudioConnection();
        }
    }

    private void playHelloMusic(TextChannel channel, String trackUrl) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        connectToFirstVoiceChannel(channel.getGuild().getAudioManager());

        playerManager.loadItemOrdered(getGuildAudioPlayer(channel.getGuild()), trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                musicManager.scheduler.hello(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {

            }

            @Override
            public void noMatches() {
            }

            @Override
            public void loadFailed(FriendlyException exception) {
            }
        });
    }

    private void playByeMusic(TextChannel channel, String trackUrl) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        playerManager.loadItemOrdered(getGuildAudioPlayer(channel.getGuild()), trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                musicManager.scheduler.bye(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {

            }

            @Override
            public void noMatches() {
            }

            @Override
            public void loadFailed(FriendlyException exception) {
            }
        });
    }
}