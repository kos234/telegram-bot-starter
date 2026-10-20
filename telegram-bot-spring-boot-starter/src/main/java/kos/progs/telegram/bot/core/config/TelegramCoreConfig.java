package kos.progs.telegram.bot.core.config;

import kos.progs.telegram.bot.button.service.ButtonService;
import kos.progs.telegram.bot.command.service.CommandService;
import kos.progs.telegram.bot.core.service.BaseBot;
import kos.progs.telegram.bot.core.service.BaseBotConsumer;
import kos.progs.telegram.bot.core.service.BaseBotUpdater;
import kos.progs.telegram.bot.states.service.StateService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Configuration
@EnableAsync
@EnableConfigurationProperties(TelegramBotProperties.class)
public class TelegramCoreConfig {

    public static final String TELEGRAM_BOT_EXECUTOR_NAME = "telegramBotExecutor";

    @Bean(name = TELEGRAM_BOT_EXECUTOR_NAME)
    @ConditionalOnMissingBean(name = TELEGRAM_BOT_EXECUTOR_NAME)
    public AsyncTaskExecutor telegramBotExecutor() {
        ThreadFactory threadFactory = Thread.ofVirtual().name("tg-bot-vt-", 1).factory();
        return new TaskExecutorAdapter(Executors.newThreadPerTaskExecutor(threadFactory));
    }

    private String resolveToken(TelegramBotProperties properties, String fallbackToken) {
        String token = properties.getToken();
        if (token == null || token.isBlank()) {
            token = fallbackToken;
        }
        return token;
    }

    @Bean
    @ConditionalOnMissingBean(TelegramClient.class)
    public TelegramClient telegramClient(TelegramBotProperties properties,
                                         @Value("${bot.token:${bot_token:${BOT_TOKEN:}}}") String fallbackToken) {
        String token = resolveToken(properties, fallbackToken);
        if (token == null || token.isBlank()) {
            token = "DUMMY_TOKEN";
        }
        return new OkHttpTelegramClient(token);
    }

    @Bean
    @ConditionalOnMissingBean(BaseBotUpdater.class)
    public BaseBotUpdater baseBotUpdater(CommandService commandService,
                                         StateService stateService,
                                         ButtonService buttonService) {
        return new BaseBotUpdater(commandService, stateService, buttonService);
    }

    @Bean
    @ConditionalOnMissingBean(BaseBotConsumer.class)
    public BaseBotConsumer baseBotConsumer(BaseBotUpdater baseBotUpdater) {
        return new BaseBotConsumer(baseBotUpdater);
    }

    @Bean
    @ConditionalOnMissingBean(SpringLongPollingBot.class)
    public SpringLongPollingBot springLongPollingBot(TelegramBotProperties properties,
                                                     @Value("${bot.token:${bot_token:${BOT_TOKEN:}}}") String fallbackToken,
                                                     LongPollingUpdateConsumer updateConsumer) {
        String token = resolveToken(properties, fallbackToken);
        if (token == null || token.isBlank()) {
            token = "DUMMY_TOKEN";
        }
        return new BaseBot(token, updateConsumer);
    }
}
