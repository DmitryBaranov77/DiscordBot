package dimon.bot.listeners;

import discord4j.core.event.domain.message.MessageUpdateEvent;
import reactor.core.publisher.Mono;

public class MessageUpdateListener extends MessageListener implements EventListener<MessageUpdateEvent>{

    @Override
    public Class<MessageUpdateEvent> getEventType() {
        return MessageUpdateEvent.class;
    }

    @Override
    public Mono<Void> execute(MessageUpdateEvent event) {
        return Mono.just(event)
                .filter(MessageUpdateEvent::isContentChanged)
                .flatMap(MessageUpdateEvent::getMessage)
                .flatMap(super::processCommand);
    }
}
