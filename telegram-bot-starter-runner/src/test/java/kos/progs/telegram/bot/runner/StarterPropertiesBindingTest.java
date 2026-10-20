package kos.progs.telegram.bot.runner;

import kos.progs.telegram.bot.core.config.TelegramBotProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = StarterRunnerApplication.class, properties = {
        "telegram.bot.state-storage=memory",
        "telegram.bot.user-storage=memory",
        "telegram.bot.admin-storage=memory",
        "bot.payment.tinkoff.storage=memory",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.autoconfigure.exclude=org.telegram.telegrambots.longpolling.starter.TelegramBotStarterConfiguration"
})
@Import(StarterPropertiesBindingTest.TestDatabaseConfig.class)
class StarterPropertiesBindingTest {

    @TestConfiguration
    static class TestDatabaseConfig {
        @Bean
        @Primary
        public DataSource dataSource() throws Exception {
            DataSource ds = Mockito.mock(DataSource.class);
            Connection conn = Mockito.mock(Connection.class);
            DatabaseMetaData meta = Mockito.mock(DatabaseMetaData.class);
            Mockito.when(ds.getConnection()).thenReturn(conn);
            Mockito.when(ds.getConnection(Mockito.any(), Mockito.any())).thenReturn(conn);
            Mockito.when(conn.getMetaData()).thenReturn(meta);
            Mockito.when(conn.getAutoCommit()).thenReturn(true);
            return ds;
        }

        @Bean
        @Primary
        public TelegramClient telegramClient() {
            return Mockito.mock(TelegramClient.class);
        }
    }

    @Autowired
    private TelegramBotProperties properties;

    @Test
    @DisplayName("Verify all telegram.bot properties are loaded and enabled")
    void testTelegramBotPropertiesLoaded() {
        assertThat(properties).isNotNull();
        assertThat(properties.getToken()).isNotBlank();

        // Language properties
        assertThat(properties.getLanguage().isEnabled()).isTrue();
        assertThat(properties.getLanguage().getDefaultCode()).isEqualTo("RU");
        assertThat(properties.getLanguage().getCustom()).isNotEmpty();
        var customLang = properties.getLanguage().getCustom().getFirst();
        assertThat(customLang.getCode()).isEqualTo("KZ");
        assertThat(customLang.getTag()).isEqualTo("kk");
        assertThat(customLang.getTitle()).isEqualTo("Қазақша");

        // Currency properties
        assertThat(properties.getCurrency().isEnabled()).isTrue();
        assertThat(properties.getCurrency().getDefaultCode()).isEqualTo("RUB");
        assertThat(properties.getCurrency().getCustom()).isNotEmpty();
        var customCurrency = properties.getCurrency().getCustom().getFirst();
        assertThat(customCurrency.getCode()).isEqualTo("KZT");
        assertThat(customCurrency.getTitle()).isEqualTo("Тенге");

        // Email & Phone properties
        assertThat(properties.getEmail().isEnabled()).isTrue();
        assertThat(properties.getPhone().isEnabled()).isTrue();
    }
}
