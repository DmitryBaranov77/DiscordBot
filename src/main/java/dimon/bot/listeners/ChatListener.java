package dimon.bot.listeners;

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
public class ChatListener  extends ListenerAdapter implements EventListener {
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
            case "~notify":
                if(event.getAuthor().getId().equals("407644589277708298") || event.getAuthor().getId().equals("286154555417296906") || event.getAuthor().getId().equals("587897463252189195")){
                    event.getChannel().sendMessage("Raccoona_gg Запустила трансляцию! Залетай скорее! https://www.twitch.tv/raccoona_gg").queue();
                } else{
                    event.getChannel().sendMessage("Отказано в доступе. ХЫ").queue();
                }
                break;
            case "~id":
                System.out.println(event.getChannel().getId());
        }
        if ("~play".equals(command[0]) && command.length == 2) {
            musicListener.loadAndPlay(event.getMember().getVoiceState().getChannel(), event.getChannel(), command[1]);
        }

        super.onGuildMessageReceived(event);
    }

    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        TextChannel channel = event.getChannelJoined().getGuild().getDefaultChannel();
        if (!event.getMember().getUser().isBot()) {
            musicListener.playHelloMusic(event.getChannelJoined(), channel, "http://192.168.1.74:9000/discord/oprivet.mp3");
        } else {
            musicListener.playHelloMusic(event.getChannelJoined(), channel, "http://192.168.1.74:9000/discord/shizofreniya.mp3");
            musicListener.playHelloMusic(event.getChannelJoined(), channel, "http://192.168.1.74:9000/discord/-blin-zachem-ya-syuda-prishel.mp3");
            musicListener.playHelloMusic(event.getChannelJoined(), channel, "http://192.168.1.74:9000/discord/povezlo-povezlo.mp3");
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
