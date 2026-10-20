package kos.progs.telegram.bot.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import kos.progs.telegram.bot.admin.config.AdminConfig;
import kos.progs.telegram.bot.admin.repository.AdminsRepository;
import kos.progs.telegram.bot.admin.repository.jpa.adapters.AdminRepositoryAdapter;
import kos.progs.telegram.bot.admin.repository.jpa.repository.JPAAdminUserRepository;
import kos.progs.telegram.bot.admin.repository.memory.MemoryAdminsRepository;
import kos.progs.telegram.bot.i18n.service.MessagesService;
import kos.progs.telegram.bot.states.config.StateConfig;
import kos.progs.telegram.bot.states.repository.HistoryRepository;
import kos.progs.telegram.bot.states.repository.StateRepository;
import kos.progs.telegram.bot.states.repository.memory.MemoryHistoryRepository;
import kos.progs.telegram.bot.states.repository.memory.MemoryStateRepository;
import kos.progs.telegram.bot.states.repository.redis.RedisHistoryRepository;
import kos.progs.telegram.bot.states.repository.redis.RedisStateRepository;
import kos.progs.telegram.bot.user.config.UserConfig;
import kos.progs.telegram.bot.user.repository.IUserInfoRepository;
import kos.progs.telegram.bot.user.repository.jpa.JPAUserInfoRepository;
import kos.progs.telegram.bot.user.repository.jpa.UserInfoRepositoryAdapter;
import kos.progs.telegram.bot.user.repository.memory.MemoryUserInfoRepository;
import kos.progs.telegram.bot.user.service.UserInfoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static org.assertj.core.api.Assertions.assertThat;

class StorageConfigurationDefaultsTest {

    private final ApplicationContextRunner stateContextRunner = new ApplicationContextRunner()
            .withUserConfiguration(StateConfig.class)
            .withBean(ObjectMapper.class, ObjectMapper::new)
            .withBean(StringRedisTemplate.class, () -> Mockito.mock(StringRedisTemplate.class))
            .withBean(UserInfoService.class, () -> Mockito.mock(UserInfoService.class));

    private final ApplicationContextRunner userContextRunner = new ApplicationContextRunner()
            .withUserConfiguration(UserConfig.class)
            .withBean(TelegramBotProperties.class, TelegramBotProperties::new)
            .withBean(JPAUserInfoRepository.class, () -> Mockito.mock(JPAUserInfoRepository.class));

    private final ApplicationContextRunner adminContextRunner = new ApplicationContextRunner()
            .withUserConfiguration(AdminConfig.class)
            .withBean(JPAAdminUserRepository.class, () -> Mockito.mock(JPAAdminUserRepository.class))
            .withBean(TelegramClient.class, () -> Mockito.mock(TelegramClient.class))
            .withBean(MessagesService.class, () -> Mockito.mock(MessagesService.class));

    @Test
    @DisplayName("TelegramBotProperties has redis for state and jpa for user and admin by default")
    void testTelegramBotPropertiesDefaults() {
        TelegramBotProperties properties = new TelegramBotProperties();
        assertThat(properties.getStateStorage()).isEqualTo("redis");
        assertThat(properties.getUserStorage()).isEqualTo("jpa");
        assertThat(properties.getAdminStorage()).isEqualTo("jpa");
    }

    @Test
    @DisplayName("StateConfig activates RedisStorageConfiguration by default when no property is provided")
    void testStateStorageDefaultsToRedis() {
        stateContextRunner.run(context -> {
            assertThat(context).hasSingleBean(StateRepository.class);
            assertThat(context).hasSingleBean(HistoryRepository.class);
            assertThat(context.getBean(StateRepository.class)).isInstanceOf(RedisStateRepository.class);
            assertThat(context.getBean(HistoryRepository.class)).isInstanceOf(RedisHistoryRepository.class);
        });
    }

    @Test
    @DisplayName("StateConfig activates MemoryStorageConfiguration when explicitly requested")
    void testStateStorageCanBeConfiguredToMemory() {
        stateContextRunner
                .withPropertyValues("telegram.bot.state-storage=memory")
                .run(context -> {
                    assertThat(context).hasSingleBean(StateRepository.class);
                    assertThat(context).hasSingleBean(HistoryRepository.class);
                    assertThat(context.getBean(StateRepository.class)).isInstanceOf(MemoryStateRepository.class);
                    assertThat(context.getBean(HistoryRepository.class)).isInstanceOf(MemoryHistoryRepository.class);
                });
    }

    @Test
    @DisplayName("UserConfig activates JpaUserStorageConfiguration by default when no property is provided")
    void testUserStorageDefaultsToJpa() {
        userContextRunner.run(context -> {
            assertThat(context).hasSingleBean(IUserInfoRepository.class);
            assertThat(context.getBean(IUserInfoRepository.class)).isInstanceOf(UserInfoRepositoryAdapter.class);
        });
    }

    @Test
    @DisplayName("UserConfig activates MemoryUserStorageConfiguration when explicitly requested")
    void testUserStorageCanBeConfiguredToMemory() {
        userContextRunner
                .withPropertyValues("telegram.bot.user-storage=memory")
                .run(context -> {
                    assertThat(context).hasSingleBean(IUserInfoRepository.class);
                    assertThat(context.getBean(IUserInfoRepository.class)).isInstanceOf(MemoryUserInfoRepository.class);
                });
    }

    @Test
    @DisplayName("AdminConfig activates JpaAdminStorageConfiguration by default when no property is provided")
    void testAdminStorageDefaultsToJpa() {
        adminContextRunner.run(context -> {
            assertThat(context).hasSingleBean(AdminsRepository.class);
            assertThat(context.getBean(AdminsRepository.class)).isInstanceOf(AdminRepositoryAdapter.class);
        });
    }

    @Test
    @DisplayName("AdminConfig activates MemoryAdminStorageConfiguration when explicitly requested")
    void testAdminStorageCanBeConfiguredToMemory() {
        adminContextRunner
                .withPropertyValues("telegram.bot.admin-storage=memory")
                .run(context -> {
                    assertThat(context).hasSingleBean(AdminsRepository.class);
                    assertThat(context.getBean(AdminsRepository.class)).isInstanceOf(MemoryAdminsRepository.class);
                });
    }
}
