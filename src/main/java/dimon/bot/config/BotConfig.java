package dimon.bot.config;

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


}
