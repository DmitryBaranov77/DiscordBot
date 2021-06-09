package dimon.bot.listeners;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Service;

@Service
public class MusicListener extends ListenerAdapter implements EventListener {
    @Override
    public void onMessageReceived(MessageReceivedEvent event){
        Message msg = event.getMessage();
        if(msg.getContentRaw().equalsIgnoreCase("а")){
            event.getChannel().sendMessage("Хуй на").queue();
        }
    }
}
