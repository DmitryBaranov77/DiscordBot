package dimon.bot.config;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class BotConfig {

    private static final Logger LOG = LoggerFactory.getLogger(BotConfig.class);

    @Value("${discord.bot.token}")
    private String token;

    @Value("${access.token}")
    private String accessToken;

    @Value("${client.id}")
    private String clientId;

    @Value("${client.secret}")
    private String clientSecret;
    private OAuth2Credential credential;

    @Bean
    public JDA jda(List<EventListener> eventListeners){
        LOG.info("Starting JDA");
        JDA jda = null;
        try {
            jda = JDABuilder.createDefault(token)
                    .addEventListeners(eventListeners.toArray())
                    .build();
            LOG.info("JDA status: {}", jda.getStatus());
        }catch (Exception e){
            LOG.error("Something went wrong", e);
        }

        return jda;
    }

    @Bean
    public TwitchClient client(){
        credential = new OAuth2Credential("twitch", accessToken);
        TwitchClient twitchClient = TwitchClientBuilder.builder()
                .withClientId(clientId)
                .withClientSecret(clientSecret)
                .withEnableHelix(true)
                .withChatAccount(credential)
                .withEnableChat(true)
                .withEnableKraken(true)
                .build();

        twitchClient.getClientHelper().enableStreamEventListener("sergio270");
        twitchClient.getChat().joinChannel("sergio270");
        return twitchClient;
    }
}
