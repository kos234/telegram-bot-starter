package kos.progs.telegram.bot.i18n.config;

import kos.progs.telegram.bot.common.config.CustomLangAndCurrencyInitializer;
import kos.progs.telegram.bot.common.model.Currency;
import kos.progs.telegram.bot.common.model.Lang;
import kos.progs.telegram.bot.core.config.TelegramBotProperties;
import kos.progs.telegram.bot.i18n.service.MessagesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.List;

@AutoConfiguration
public class I18nConfig {

    @Bean
    @ConditionalOnMissingBean(MessageSource.class)
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames("messages", "starter_messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setFallbackToSystemLocale(false);
        return messageSource;
    }

    @Bean
    @ConditionalOnMissingBean(MessagesService.class)
    public MessagesService messagesService(MessageSource messageSource) {
        return new MessagesService(messageSource);
    }

    @Bean
    @ConditionalOnMissingBean(CustomLangAndCurrencyInitializer.class)
    public CustomLangAndCurrencyInitializer customLangAndCurrencyInitializer(
            TelegramBotProperties properties,
            @Autowired(required = false) List<Lang> customLangBeans,
            @Autowired(required = false) List<Currency> customCurrencyBeans) {
        return new CustomLangAndCurrencyInitializer(properties, customLangBeans, customCurrencyBeans);
    }
}
