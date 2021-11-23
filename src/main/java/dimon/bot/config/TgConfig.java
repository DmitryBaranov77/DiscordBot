package dimon.bot.config;

import com.pengrad.telegrambot.TelegramBot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TgConfig {

    private static String tgBotToken;
    private static TelegramBot telegramBot = null;

    public static TelegramBot getTelegramBot(){
        if (telegramBot == null){
            telegramBot = new TelegramBot(tgBotToken);
            System.out.println(tgBotToken);
        }
        return telegramBot;
    }

    @Value("${tg.bot.token}")
    public void setToken(String token){
        tgBotToken = token;
    }
}
