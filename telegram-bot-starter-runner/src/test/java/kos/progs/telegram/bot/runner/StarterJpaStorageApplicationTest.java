package kos.progs.telegram.bot.runner;

import kos.progs.telegram.bot.admin.repository.AdminsRepository;
import kos.progs.telegram.bot.admin.repository.jpa.adapters.AdminRepositoryAdapter;
import kos.progs.telegram.bot.admin.service.AdminService;
import kos.progs.telegram.bot.payment.tinkoff.repository.TinkoffRepository;
import kos.progs.telegram.bot.payment.tinkoff.repository.jpa.adapters.JPATinkoffPaymentRepositoryAdapter;
import kos.progs.telegram.bot.states.repository.StateRepository;
import kos.progs.telegram.bot.states.repository.jpa.adapters.StateRepositoryAdapter;
import kos.progs.telegram.bot.states.service.StateService;
import kos.progs.telegram.bot.user.repository.IUserInfoRepository;
import kos.progs.telegram.bot.user.repository.jpa.UserInfoRepositoryAdapter;
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
        "telegram.bot.state-storage=jpa",
        "telegram.bot.user-storage=jpa",
        "telegram.bot.admin-storage=jpa",
        "bot.payment.tinkoff.storage=jpa",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.autoconfigure.exclude=org.telegram.telegrambots.longpolling.starter.TelegramBotStarterConfiguration"
})
@Import(StarterJpaStorageApplicationTest.TestDatabaseConfig.class)
class StarterJpaStorageApplicationTest {

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
            Mockito.when(meta.getDatabaseProductName()).thenReturn("PostgreSQL");
            Mockito.when(meta.getDatabaseProductVersion()).thenReturn("13.0");
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
    @DisplayName("Verify JPA repository adapters and services load cleanly in JPA storage mode")
    void testJpaStorageBeansLoaded() {
        // Verify JPA repository adapters
        AdminsRepository adminsRepo = context.getBean(AdminsRepository.class);
        assertThat(adminsRepo).isInstanceOf(AdminRepositoryAdapter.class);

        IUserInfoRepository userRepo = context.getBean(IUserInfoRepository.class);
        assertThat(userRepo).isInstanceOf(UserInfoRepositoryAdapter.class);

        StateRepository stateRepo = context.getBean(StateRepository.class);
        assertThat(stateRepo).isInstanceOf(StateRepositoryAdapter.class);

        TinkoffRepository tinkoffRepo = context.getBean(TinkoffRepository.class);
        assertThat(tinkoffRepo).isInstanceOf(JPATinkoffPaymentRepositoryAdapter.class);

        // Verify services with injected JPA adapters
        assertThat(context.getBean(AdminService.class)).isNotNull();
        assertThat(context.getBean(UserInfoService.class)).isNotNull();
        assertThat(context.getBean(StateService.class)).isNotNull();
    }
}
