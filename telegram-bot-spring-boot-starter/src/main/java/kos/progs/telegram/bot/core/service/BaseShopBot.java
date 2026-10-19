package kos.progs.telegram.bot.core.service;

import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;

@Deprecated
public class BaseShopBot extends BaseBot {
    public BaseShopBot(String apiToken, LongPollingUpdateConsumer updateConsumer) {
        super(apiToken, updateConsumer);
    }
}
