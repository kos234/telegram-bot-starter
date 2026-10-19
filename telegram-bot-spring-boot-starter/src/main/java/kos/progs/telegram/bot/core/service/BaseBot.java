package kos.progs.telegram.bot.core.service;

import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;

@RequiredArgsConstructor
public class BaseBot implements SpringLongPollingBot {
    private final String apiToken;
    private final LongPollingUpdateConsumer updateConsumer;

    @Override
    public String getBotToken() {
        return apiToken;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return updateConsumer;
    }
}
