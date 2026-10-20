package kos.progs.telegram.bot.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "telegram.bot")
@Getter
@Setter
public class TelegramBotProperties {
    /**
     * Токен бота Telegram.
     */
    private String token;

    /**
     * Хранилище FSM состояний и истории навигации: redis (по умолчанию), memory, jpa.
     */
    private String stateStorage = "redis";

    /**
     * Хранилище данных пользователей: jpa (по умолчанию), memory, redis.
     */
    private String userStorage = "jpa";

    /**
     * Хранилище администраторов: jpa (по умолчанию), memory.
     */
    private String adminStorage = "jpa";

    /**
     * Настройки поддержки языков.
     */
    private LanguageProperties language = new LanguageProperties();

    /**
     * Настройки поддержки валют.
     */
    private CurrencyProperties currency = new CurrencyProperties();

    /**
     * Настройки поддержки email.
     */
    private EmailProperties email = new EmailProperties();

    /**
     * Настройки поддержки телефона.
     */
    private PhoneProperties phone = new PhoneProperties();

    @Getter
    @Setter
    public static class LanguageProperties {
        /**
         * Включена ли поддержка смены языков пользователем.
         */
        private boolean enabled = true;

        /**
         * Код языка по умолчанию (например, RU, EN).
         */
        private String defaultCode = "RU";

        /**
         * Список дополнительных языков для регистрации.
         */
        private List<CustomLanguageProperties> custom = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class CustomLanguageProperties {
        private byte id;
        private String code;
        private String tag;
        private String title;
    }

    @Getter
    @Setter
    public static class CurrencyProperties {
        /**
         * Включена ли поддержка валют и их смены пользователем.
         */
        private boolean enabled = true;

        /**
         * Код валюты по умолчанию (например, RUB, USD).
         */
        private String defaultCode = "RUB";

        /**
         * Список дополнительных валют для регистрации.
         */
        private List<CustomCurrencyProperties> custom = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class CustomCurrencyProperties {
        private byte id;
        private String code;
        private String title;
    }

    @Getter
    @Setter
    public static class EmailProperties {
        /**
         * Включена ли строка и возможность смены email в настройках.
         */
        private boolean enabled = true;
    }

    @Getter
    @Setter
    public static class PhoneProperties {
        /**
         * Включена ли строка и возможность смены телефона в настройках.
         */
        private boolean enabled = true;
    }
}
