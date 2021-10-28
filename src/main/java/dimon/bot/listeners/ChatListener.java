package dimon.bot.listeners;

import lombok.Getter;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Service;

@Service
public class ChatListener extends ListenerAdapter implements EventListener {
    @Getter
    private MusicListener musicListener;

    public ChatListener(MusicListener musicListener) {
        this.musicListener = musicListener;
    }

    @SneakyThrows
    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        String[] command = event.getMessage().getContentRaw().split(" ", 2);
        switch (command[0]) {
            case "~skip":
                musicListener.skipTrack(event.getChannel());
                break;
            case "~leave":
                musicListener.playByeMusic(event.getChannel(), "http://192.168.1.74:9000/discord/kto-kuda-a-ya-po-delam.mp3");
                musicListener.disconnectFromVoiceChannel(event.getGuild().getAudioManager());
                break;
            case "~join":
                musicListener.connectToVoiceChannel(event.getMember().getVoiceState().getChannel(), event.getChannel().getGuild().getAudioManager());
                break;
        }
        if ("~play".equals(command[0]) && command.length == 2) {
            musicListener.loadAndPlay(event.getMember().getVoiceState().getChannel(), event.getChannel(), command[1]);
        }

        super.onGuildMessageReceived(event);
    }

    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        TextChannel channel = event.getChannelJoined().getGuild().getDefaultChannel();
        if(!event.getMember().getUser().isBot()){
            musicListener.connectToVoiceChannel(event.getChannelJoined(), event.getGuild().getAudioManager());
        }
        if (!event.getMember().getUser().isBot() && (event.getChannelJoined() == event.getGuild().getAudioManager().getConnectedChannel())) {
            musicListener.playHelloMusic(event.getChannelJoined(), channel, "http://192.168.1.74:9000/discord/oprivet.mp3");
        }
    }

    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        VoiceChannel channelLeft = event.getChannelLeft();
        if (channelLeft == event.getGuild().getAudioManager().getConnectedChannel() && channelLeft.getMembers().size() == 1) {
            musicListener.getGuildAudioPlayer(event.getGuild()).scheduler.clearQueue();
            event.getGuild().getAudioManager().closeAudioConnection();
        }
    }
}
