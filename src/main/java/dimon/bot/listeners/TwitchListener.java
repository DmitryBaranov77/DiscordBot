package dimon.bot.listeners;

import com.github.philippheuer.events4j.core.EventManager;
import com.github.philippheuer.events4j.simple.SimpleEventHandler;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.events.ChannelGoLiveEvent;
import com.github.twitch4j.events.ChannelGoOfflineEvent;
import dimon.bot.listeners.services.SendInfoMessage;
import net.dv8tion.jda.api.JDA;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class TwitchListener {
    private TwitchClient twitchClient;
    private ScheduledExecutorService se;
    private JDA jda;

    public TwitchListener(TwitchClient twitchClient, JDA jda) {
        this.twitchClient = twitchClient;
        this.jda = jda;
        register();
    }

    private void register(){
        EventManager eventManager = twitchClient.getEventManager();
        SimpleEventHandler eventHandler = twitchClient.getEventManager().getEventHandler(SimpleEventHandler.class);
        eventHandler.onEvent(ChannelMessageEvent.class, event -> onChannelMessage(event));
        eventHandler.onEvent(ChannelGoLiveEvent.class, event -> onLiveEvent(event));
        eventHandler.onEvent(ChannelGoOfflineEvent.class, event -> onOfflineEvent(event));
    }

    private void onLiveEvent(ChannelGoLiveEvent event){
        jda.getTextChannelById("872417764193742911").sendMessage("@\uD83D\uDD34Сабчики\uD83D\uDD34 Raccoona_gg Запустила трансляцию! Залетай скорее! https://www.twitch.tv/raccoona_gg").queue();
        twitchClient.getChat().sendMessage(event.getChannel().getName(), "Удачного стрима красотка!");
        se = Executors.newScheduledThreadPool(1);
        se.scheduleAtFixedRate(new SendInfoMessage(twitchClient, event.getChannel().getName()), 0, 5, TimeUnit.MINUTES);
    }

    private void onOfflineEvent(ChannelGoOfflineEvent event){
        twitchClient.getChat().sendMessage(event.getChannel().getName(), "Пока пока");
        se.shutdownNow();
    }

    private void onChannelMessage(ChannelMessageEvent event){
        String msg = event.getMessage();
        switch (msg) {
            case "а":
                event.getTwitchChat().sendMessage(event.getChannel().getName(), event.getUser().getName() + " хуй на");
                break;
            case "аа":
                event.getTwitchChat().sendMessage(event.getChannel().getName(), event.getUser().getName() + " хуй на");
                break;
            case "ааа":
                event.getTwitchChat().sendMessage(event.getChannel().getName(), event.getUser().getName() + " хуй на");
                break;
            case "!start":
                se = Executors.newScheduledThreadPool(1);
                se.scheduleAtFixedRate(new SendInfoMessage(twitchClient, event.getChannel().getName()), 0, 5, TimeUnit.MINUTES);
                break;
            case "!stop":
                se.shutdownNow();
                break;
            case "!см":
                event.getTwitchChat().sendMessage(event.getChannel().getName(), event.getUser().getName()+" Размер твоего меча Экскалибура "+
                        ( (int) (1 + Math.random() * 30))+" см.");
                break;
        }
    }

}
