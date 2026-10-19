package kos.progs.telegram.bot.command.config;

import kos.progs.telegram.bot.admin.service.AdminService;
import kos.progs.telegram.bot.command.handler.CancelHandler;
import kos.progs.telegram.bot.command.handler.CommandHandler;
import kos.progs.telegram.bot.command.handler.StartHandler;
import kos.progs.telegram.bot.command.service.CommandService;
import kos.progs.telegram.bot.i18n.service.MessagesService;
import kos.progs.telegram.bot.main_menu.MainMenuContributor;
import kos.progs.telegram.bot.states.service.NavigationService;
import kos.progs.telegram.bot.states.service.StateService;
import kos.progs.telegram.bot.user.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Collections;
import java.util.List;

@Configuration
public class CommandConfig {

    @Bean
    @ConditionalOnMissingBean(StartHandler.class)
    public StartHandler startHandler(TelegramClient telegramClient,
                                     NavigationService navigationService,
                                     MessagesService messagesService,
                                     AdminService adminService,
                                     @Autowired(required = false) List<MainMenuContributor> contributors) {
        return new StartHandler(telegramClient, navigationService, messagesService, adminService, contributors);
    }

    @Bean
    @ConditionalOnMissingBean(CancelHandler.class)
    public CancelHandler cancelHandler(NavigationService navigationService,
                                       ApplicationEventPublisher applicationEventPublisher,
                                       MessagesService messagesService) {
        return new CancelHandler(navigationService, applicationEventPublisher, messagesService);
    }

    @Bean
    @ConditionalOnMissingBean(CommandService.class)
    public CommandService commandService(@Autowired(required = false) List<CommandHandler> commandHandlers,
                                         StateService stateService,
                                         NavigationService navigationService,
                                         UserInfoService userInfoService) {
        return new CommandService(
                commandHandlers != null ? commandHandlers : Collections.emptyList(),
                stateService,
                navigationService,
                userInfoService
        );
    }
}
