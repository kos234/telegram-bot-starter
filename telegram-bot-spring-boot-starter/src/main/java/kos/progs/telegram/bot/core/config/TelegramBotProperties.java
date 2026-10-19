package kos.progs.telegram.bot.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "telegram.bot")
@Getter
@Setter
public class TelegramBotProperties {
    /**
     * Токен бота Telegram.
     */
    private String token;

    /**
     * Хранилище FSM состояний и истории навигации: memory (по умолчанию), redis, jpa.
     */
    private String stateStorage = "memory";

    /**
     * Хранилище данных пользователей: memory (по умолчанию), redis, jpa.
     */
    private String userStorage = "memory";

    /**
     * Хранилище администраторов: memory (по умолчанию), jpa.
     */
    private String adminStorage = "memory";
}
