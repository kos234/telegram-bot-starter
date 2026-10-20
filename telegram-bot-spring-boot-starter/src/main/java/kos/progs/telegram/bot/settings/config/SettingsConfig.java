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
import kos.progs.telegram.bot.settings.spi.CurrencySettingsRowContributor;
import kos.progs.telegram.bot.settings.spi.EmailSettingsRowContributor;
import kos.progs.telegram.bot.settings.spi.LanguageSettingsRowContributor;
import kos.progs.telegram.bot.settings.spi.PhoneSettingsRowContributor;
import kos.progs.telegram.bot.settings.spi.SettingsRowContributor;
import kos.progs.telegram.bot.settings.state.UserInfoChangeStateHandler;
import kos.progs.telegram.bot.user.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

@AutoConfiguration
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
    @ConditionalOnProperty(prefix = "telegram.bot.language", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean(LanguageSettingsRowContributor.class)
    public LanguageSettingsRowContributor languageSettingsRowContributor(MessagesService messagesService,
                                                                         ChangeLangCommand changeLangCommand) {
        return new LanguageSettingsRowContributor(messagesService, changeLangCommand);
    }

    @Bean
    @ConditionalOnProperty(prefix = "telegram.bot.currency", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean(CurrencySettingsRowContributor.class)
    public CurrencySettingsRowContributor currencySettingsRowContributor(MessagesService messagesService,
                                                                         ChangeCurrencyCommand changeCurrencyCommand) {
        return new CurrencySettingsRowContributor(messagesService, changeCurrencyCommand);
    }

    @Bean
    @ConditionalOnProperty(prefix = "telegram.bot.email", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean(EmailSettingsRowContributor.class)
    public EmailSettingsRowContributor emailSettingsRowContributor(MessagesService messagesService,
                                                                   ChangeUserInfoCommand changeUserInfoCommand) {
        return new EmailSettingsRowContributor(messagesService, changeUserInfoCommand);
    }

    @Bean
    @ConditionalOnProperty(prefix = "telegram.bot.phone", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean(PhoneSettingsRowContributor.class)
    public PhoneSettingsRowContributor phoneSettingsRowContributor(MessagesService messagesService,
                                                                   ChangeUserInfoCommand changeUserInfoCommand,
                                                                   UserInfoService userInfoService) {
        return new PhoneSettingsRowContributor(messagesService, changeUserInfoCommand, userInfoService);
    }

    @Bean
    @ConditionalOnMissingBean(SettingsPresenter.class)
    public SettingsPresenter settingsPresenter(MessagesService messagesService,
                                                TelegramClient telegramClient,
                                                CancelHandler cancelHandler,
                                                @Autowired(required = false) List<SettingsRowContributor> contributors) {
        return new SettingsPresenter(
                messagesService,
                telegramClient,
                cancelHandler,
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
    @ConditionalOnProperty(prefix = "telegram.bot.language", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean(ChangeLanguageButton.class)
    public ChangeLanguageButton changeLanguageButton(UserInfoService userInfoService,
                                                     SettingsPresenter settingsPresenter,
                                                     ChangeLangCommand changeLangCommand) {
        return new ChangeLanguageButton(userInfoService, settingsPresenter, changeLangCommand);
    }

    @Bean
    @ConditionalOnProperty(prefix = "telegram.bot.currency", name = "enabled", havingValue = "true", matchIfMissing = true)
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
