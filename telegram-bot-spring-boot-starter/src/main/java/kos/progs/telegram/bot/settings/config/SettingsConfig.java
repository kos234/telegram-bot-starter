package kos.progs.telegram.bot.settings.config;

import kos.progs.telegram.bot.command.handler.CancelHandler;
import kos.progs.telegram.bot.i18n.service.MessagesService;
import kos.progs.telegram.bot.settings.button.ChangeCurrencyButton;
import kos.progs.telegram.bot.settings.button.ChangeCurrencyCommand;
import kos.progs.telegram.bot.settings.button.ChangeLangCommand;
import kos.progs.telegram.bot.settings.button.ChangeLanguageButton;
import kos.progs.telegram.bot.settings.button.ChangeUserInfoButton;
import kos.progs.telegram.bot.settings.button.ChangeUserInfoCommand;
import kos.progs.telegram.bot.settings.command.SettingsHandler;
import kos.progs.telegram.bot.settings.presenter.SettingsPresenter;
import kos.progs.telegram.bot.settings.spi.SettingsRowContributor;
import kos.progs.telegram.bot.settings.state.UserInfoChangeStateHandler;
import kos.progs.telegram.bot.user.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

@Configuration
public class SettingsConfig {

    @Bean
    @ConditionalOnMissingBean(ChangeLangCommand.class)
    public ChangeLangCommand changeLangCommand() {
        return new ChangeLangCommand();
    }

    @Bean
    @ConditionalOnMissingBean(ChangeCurrencyCommand.class)
    public ChangeCurrencyCommand changeCurrencyCommand() {
        return new ChangeCurrencyCommand();
    }

    @Bean
    @ConditionalOnMissingBean(ChangeUserInfoCommand.class)
    public ChangeUserInfoCommand changeUserInfoCommand() {
        return new ChangeUserInfoCommand();
    }

    @Bean
    @ConditionalOnMissingBean(SettingsPresenter.class)
    public SettingsPresenter settingsPresenter(MessagesService messagesService,
                                               TelegramClient telegramClient,
                                               CancelHandler cancelHandler,
                                               ChangeLangCommand changeLangCommand,
                                               ChangeCurrencyCommand changeCurrencyCommand,
                                               UserInfoService userInfoService,
                                               ChangeUserInfoCommand changeUserInfoCommand,
                                               @Autowired(required = false) List<SettingsRowContributor> contributors) {
        return new SettingsPresenter(
                messagesService,
                telegramClient,
                cancelHandler,
                changeLangCommand,
                changeCurrencyCommand,
                userInfoService,
                changeUserInfoCommand,
                contributors
        );
    }

    @Bean
    @ConditionalOnMissingBean(SettingsHandler.class)
    public SettingsHandler settingsHandler(SettingsPresenter settingsPresenter,
                                           MessagesService messagesService) {
        return new SettingsHandler(settingsPresenter, messagesService);
    }

    @Bean
    @ConditionalOnMissingBean(ChangeLanguageButton.class)
    public ChangeLanguageButton changeLanguageButton(UserInfoService userInfoService,
                                                     SettingsPresenter settingsPresenter,
                                                     ChangeLangCommand changeLangCommand) {
        return new ChangeLanguageButton(userInfoService, settingsPresenter, changeLangCommand);
    }

    @Bean
    @ConditionalOnMissingBean(ChangeCurrencyButton.class)
    public ChangeCurrencyButton changeCurrencyButton(UserInfoService userInfoService,
                                                     ChangeCurrencyCommand changeCurrencyCommand,
                                                     SettingsPresenter settingsPresenter) {
        return new ChangeCurrencyButton(userInfoService, changeCurrencyCommand, settingsPresenter);
    }

    @Bean
    @ConditionalOnMissingBean(ChangeUserInfoButton.class)
    public ChangeUserInfoButton changeUserInfoButton(MessagesService messagesService,
                                                     ChangeUserInfoCommand changeUserInfoCommand,
                                                     TelegramClient telegramClient) {
        return new ChangeUserInfoButton(messagesService, changeUserInfoCommand, telegramClient);
    }

    @Bean
    @ConditionalOnMissingBean(UserInfoChangeStateHandler.class)
    public UserInfoChangeStateHandler userInfoChangeStateHandler(UserInfoService userInfoService,
                                                                 MessagesService messagesService,
                                                                 ApplicationEventPublisher applicationEventPublisher,
                                                                 TelegramClient telegramClient) {
        return new UserInfoChangeStateHandler(userInfoService, messagesService, applicationEventPublisher, telegramClient);
    }
}
