package kos.progs.telegram.bot.i18n.config;

import kos.progs.telegram.bot.i18n.service.MessagesService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

@Configuration
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
}
