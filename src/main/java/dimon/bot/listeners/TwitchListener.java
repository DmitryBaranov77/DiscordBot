package dimon.bot.listeners;

import com.github.philippheuer.events4j.core.EventManager;
import com.github.philippheuer.events4j.simple.SimpleEventHandler;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.chat.events.channel.DonationEvent;
import com.github.twitch4j.events.ChannelGoLiveEvent;
import com.github.twitch4j.events.ChannelGoOfflineEvent;
import com.github.twitch4j.helix.domain.FollowList;
import dimon.bot.listeners.services.SendInfoMessage;
import net.dv8tion.jda.api.JDA;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
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

    private void register() {
        EventManager eventManager = twitchClient.getEventManager();
        SimpleEventHandler eventHandler = twitchClient.getEventManager().getEventHandler(SimpleEventHandler.class);
        eventHandler.onEvent(ChannelMessageEvent.class, event -> onChannelMessage(event));
        eventHandler.onEvent(ChannelGoLiveEvent.class, event -> onLiveEvent(event));
        eventHandler.onEvent(ChannelGoOfflineEvent.class, event -> onOfflineEvent(event));
        eventHandler.onEvent(DonationEvent.class, event -> onDonationEvent(event));
    }

    private void onLiveEvent(ChannelGoLiveEvent event) {
        jda.getTextChannelById("872417764193742911").sendMessage("@everyone Raccooona Запустила трансляцию! Залетай скорее! https://www.twitch.tv/raccooona").queue();
        twitchClient.getChat().sendMessage(event.getChannel().getName(), "Удачного стрима красотка!");
        se = Executors.newScheduledThreadPool(1);
        se.scheduleAtFixedRate(new SendInfoMessage(twitchClient, event.getChannel().getName()), 0, 5, TimeUnit.MINUTES);
    }

    private void onOfflineEvent(ChannelGoOfflineEvent event) {
        twitchClient.getChat().sendMessage(event.getChannel().getName(), "Пока пока");
        se.shutdownNow();
    }

    private void onDonationEvent(DonationEvent event) {
        twitchClient.getChat().sendPrivateMessage("dedrybak77", event.getUser() + " задонатил " + event.getAmount() + " " + event.getCurrency());
    }

    private void onChannelMessage(ChannelMessageEvent event) {
        String msg = event.getMessage();
        switch (msg) {
            case "а":
                event.getTwitchChat().sendMessage(event.getChannel().getName(), event.getUser().getName() + " хуй на");
                break;
            case "аа":
                event.getTwitchChat().sendMessage(event.getChannel().getName(), event.getUser().getName() + " хуй наа");
                break;
            case "ааа":
                event.getTwitchChat().sendMessage(event.getChannel().getName(), event.getUser().getName() + " хуй нааа");
                break;
            case "да":
                event.getTwitchChat().sendMessage(event.getChannel().getName(), event.getUser().getName() + " пизда");
                break;
            case "Да":
                event.getTwitchChat().sendMessage(event.getChannel().getName(), event.getUser().getName() + " Пизда");
                break;
            case "нет":
                event.getTwitchChat().sendMessage(event.getChannel().getName(), event.getUser().getName() + " говна пакет");
                break;
            case "Нет":
                event.getTwitchChat().sendMessage(event.getChannel().getName(), event.getUser().getName() + " Говна пакет");
                break;
            case "!start":
                se = Executors.newScheduledThreadPool(1);
                se.scheduleAtFixedRate(new SendInfoMessage(twitchClient, event.getChannel().getName()), 0, 5, TimeUnit.MINUTES);
                break;
            case "!см":
                event.getTwitchChat().sendMessage(event.getChannel().getName(), event.getUser().getName() + " Размер твоего меча Экскалибура " +
                        ((int) (1 + Math.random() * 30)) + " см.");
                break;
            case "!follow":
                FollowList followList = twitchClient.getHelix().getFollowers(null, event.getUser().getId(), event.getChannel().getId(), null, 1).execute();
                followList.getFollows().forEach(follow -> {
                    Duration duration = Duration.between(follow.getFollowedAtInstant(), Instant.now());
                    event.getTwitchChat().sendMessage(event.getChannel().getName(), event.getUser().getName() + " ты подписан на чанал " + duration.toDays() + " днёв");
                });
                break;
            case "!inst":
                event.getTwitchChat().sendMessage(event.getChannel().getName(), "Заходи в мой инстаграм, возможно я туда когда-нибудь что-нибудь туда залью (но это не точно) \uD83E\uDC16 https://www.instagram.com/raccoona__ ♡");
                break;
            case "!tg":
                event.getTwitchChat().sendMessage(event.getChannel().getName(), "Заходи в мой тг \uD83E\uDC16 https://t.me/raccooonaa ♡");
                break;
            case "!ds":
                event.getTwitchChat().sendMessage(event.getChannel().getName(), "Присоединяйся в наш дискорд канал, а то че как лох \uD83E\uDC16 https://discord.gg/TSUMEvWZhn ♡");
                break;
        }
    }

}
