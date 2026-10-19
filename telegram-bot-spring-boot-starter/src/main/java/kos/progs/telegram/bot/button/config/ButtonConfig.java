package kos.progs.telegram.bot.button.config;

import kos.progs.telegram.bot.button.handler.ButtonHandler;
import kos.progs.telegram.bot.button.service.ButtonService;
import kos.progs.telegram.bot.states.service.NavigationService;
import kos.progs.telegram.bot.states.service.StateService;
import kos.progs.telegram.bot.user.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Collections;
import java.util.List;

@Configuration
public class ButtonConfig {

    @Bean
    @ConditionalOnMissingBean(ButtonService.class)
    public ButtonService buttonService(@Autowired(required = false) List<ButtonHandler> buttonHandlers,
                                       UserInfoService userInfoService,
                                       StateService stateService,
                                       NavigationService navigationService,
                                       TelegramClient telegramClient) {
        return new ButtonService(
                buttonHandlers != null ? buttonHandlers : Collections.emptyList(),
                userInfoService,
                stateService,
                navigationService,
                telegramClient
        );
    }
}
