package kos.progs.telegram.bot.runner;

import kos.progs.telegram.bot.admin.service.AdminService;
import kos.progs.telegram.bot.button.service.ButtonService;
import kos.progs.telegram.bot.command.service.CommandService;
import kos.progs.telegram.bot.common.model.Currency;
import kos.progs.telegram.bot.common.model.Lang;
import kos.progs.telegram.bot.i18n.service.MessagesService;
import kos.progs.telegram.bot.runner.example.TestMainMenuContributor;
import kos.progs.telegram.bot.runner.example.TestPingCommandHandler;
import kos.progs.telegram.bot.states.service.NavigationService;
import kos.progs.telegram.bot.states.service.StateService;
import kos.progs.telegram.bot.user.service.UserInfoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
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
@Import(StarterRunnerApplicationTest.TestDatabaseConfig.class)
class StarterRunnerApplicationTest {

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
    private ApplicationContext context;

    @Test
    @DisplayName("Verify Spring Boot context loads with all core starter beans")
    void testCoreBeansLoaded() {
        assertThat(context.getBean(CommandService.class)).isNotNull();
        assertThat(context.getBean(StateService.class)).isNotNull();
        assertThat(context.getBean(NavigationService.class)).isNotNull();
        assertThat(context.getBean(ButtonService.class)).isNotNull();
        assertThat(context.getBean(UserInfoService.class)).isNotNull();
        assertThat(context.getBean(AdminService.class)).isNotNull();
        assertThat(context.getBean(MessagesService.class)).isNotNull();
    }

    @Test
    @DisplayName("Verify custom contributor and handler beans are discovered")
    void testCustomHandlersLoaded() {
        assertThat(context.getBean(TestPingCommandHandler.class)).isNotNull();
        assertThat(context.getBean(TestMainMenuContributor.class)).isNotNull();
    }

    @Test
    @DisplayName("Verify custom languages and currencies are initialized in registries")
    void testCustomLangAndCurrencyRegistered() {
        Lang customLang = Lang.valueOf("KZ");
        assertThat(customLang).isNotNull();
        assertThat(customLang.getTitle()).isEqualTo("Қазақша");

        Currency customCurrency = Currency.valueOf("KZT");
        assertThat(customCurrency).isNotNull();
        assertThat(customCurrency.getTitle()).isEqualTo("Тенге");
    }
}
