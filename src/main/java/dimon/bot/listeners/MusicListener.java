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
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MusicListener extends ListenerAdapter implements EventListener {

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
        VoiceChannel voiceChannel = event.getMember().getVoiceState().getChannel();
        if ("~play".equals(command[0]) && command.length == 2) {
            loadAndPlay(event.getChannel(), command[1]);
        } else if ("~skip".equals(command[0])) {
            skipTrack(event.getChannel());
        } else if("~leave".equals(command[0])){
            GuildMusicManager musicManager = getGuildAudioPlayer(event.getChannel().getGuild());
            musicManager.scheduler.clearQueue();
            playStaticMusic(event.getChannel(), "src/main/resources/static/kto-kuda-a-ya-po-delam.mp3");
            Thread.sleep(4000);
            disconnectFromVoiceChannel(event.getChannel().getGuild().getAudioManager());
        }else if("~join".equals(command[0])){
            connectToFirstVoiceChannel(event.getChannel().getGuild().getAudioManager());
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
            for (VoiceChannel voiceChannel : audioManager.getGuild().getVoiceChannels()) {
                audioManager.openAudioConnection(voiceChannel);
                audioManager.setSpeakingMode(SpeakingMode.SOUNDSHARE);
                audioManager.setSelfDeafened(true);

                break;
            }
        }
    }

    private static void disconnectFromVoiceChannel(AudioManager audioManager){
        if(audioManager.isConnected()){
            audioManager.closeAudioConnection();
        }
    }

    private static void hello(){

    }

    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        TextChannel channel = event.getChannelJoined().getGuild().getDefaultChannel();

        String trackUrl = null;
        if(!event.getMember().getUser().isBot()) {
            playStaticMusic(channel, "src/main/resources/static/o-privet.mp3");
        }else{
            playStaticMusic(channel, "src/main/resources/static/shizofreniya.mp3");
            playStaticMusic(channel, "src/main/resources/static/-blin-zachem-ya-syuda-prishel.mp3");
            playStaticMusic(channel, "src/main/resources/static/povezlo-povezlo.mp3");
        }

    }

    private void playStaticMusic(TextChannel channel, String trackUrl){
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        playerManager.loadItemOrdered(getGuildAudioPlayer(channel.getGuild()), trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                connectToFirstVoiceChannel(channel.getGuild().getAudioManager());
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
}