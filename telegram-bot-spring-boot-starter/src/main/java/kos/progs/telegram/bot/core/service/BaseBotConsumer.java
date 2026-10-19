package kos.progs.telegram.bot.core.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BaseBotConsumer implements LongPollingUpdateConsumer, ApplicationListener<BaseBotConsumer.NewUpdateEvent> {
    private final BaseBotUpdater baseBotUpdater;

    @Override
    public void consume(List<Update> updates) {
        if (updates != null) {
            updates.forEach(baseBotUpdater::update);
        }
    }

    @Override
    public void onApplicationEvent(NewUpdateEvent event) {
        if (event != null && event.getUpdate() != null) {
            baseBotUpdater.update(event.getUpdate());
        }
    }

    @Getter
    public static class NewUpdateEvent extends ApplicationEvent {
        private final Update update;

        public NewUpdateEvent(Object source, Update update) {
            super(source);
            this.update = update;
        }
    }
}
