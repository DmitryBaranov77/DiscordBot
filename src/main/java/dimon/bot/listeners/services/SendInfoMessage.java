package dimon.bot.listeners.services;

import com.github.twitch4j.TwitchClient;

public class SendInfoMessage implements Runnable{
    private TwitchClient twitchClient;
    private String channel;
    private int count;

    public SendInfoMessage(TwitchClient twitchClient, String channel) {
        this.twitchClient = twitchClient;
        this.channel = channel;
        count = 0;
    }

    @Override
    public void run() {
        twitchClient.getChat().sendMessage(channel, message());
    }

    private String message(){
        String msg = "";
        switch (count){
            case 0:
                msg = "Присоединяйся в наш дискорд канал, а то че как лох \uD83E\uDC16 https://discord.gg/TSUMEvWZhn ♡";
                count++;
                break;
            case 1:
                msg = "Заходи в мой инстаграм, возможно я туда когда-нибудь что-нибудь туда залью (но это не точно) \uD83E\uDC16 https://www.instagram.com/raccoona__ ♡";
                count++;
                break;
            case 2:
                msg = "Ты можешь сделать стримера чуточку счастливее, поддержав его канал \uD83E\uDC16 https://www.donationalerts.com/r/raccooona ♡";
                count = 0;
                break;
        }
        return msg;
    }
}
