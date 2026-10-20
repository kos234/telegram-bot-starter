package kos.progs.telegram.bot.states.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import kos.progs.telegram.bot.core.config.TelegramCoreConfig;
import kos.progs.telegram.bot.states.handler.StateHandler;
import kos.progs.telegram.bot.states.repository.HistoryRepository;
import kos.progs.telegram.bot.states.repository.StateRepository;
import kos.progs.telegram.bot.states.repository.jpa.adapters.HistoryRepositoryAdapter;
import kos.progs.telegram.bot.states.repository.jpa.adapters.StateRepositoryAdapter;
import kos.progs.telegram.bot.states.repository.jpa.repository.JPAHistoryRepository;
import kos.progs.telegram.bot.states.repository.jpa.repository.JPAStateRepository;
import kos.progs.telegram.bot.states.repository.memory.MemoryHistoryRepository;
import kos.progs.telegram.bot.states.repository.memory.MemoryStateRepository;
import kos.progs.telegram.bot.states.repository.redis.RedisHistoryRepository;
import kos.progs.telegram.bot.states.repository.redis.RedisStateRepository;
import kos.progs.telegram.bot.states.service.NavigationService;
import kos.progs.telegram.bot.states.service.StateService;
import kos.progs.telegram.bot.user.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Collections;
import java.util.List;

@AutoConfiguration(after = {TelegramCoreConfig.class}, afterName = "org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration")
public class StateConfig {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(name = "telegram.bot.state-storage", havingValue = "memory")
    public static class MemoryStorageConfiguration {
        @Bean
        @ConditionalOnMissingBean(StateRepository.class)
        public StateRepository memoryStateRepository() {
            return new MemoryStateRepository();
        }

        @Bean
        @ConditionalOnMissingBean(HistoryRepository.class)
        public HistoryRepository memoryHistoryRepository() {
            return new MemoryHistoryRepository();
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(StringRedisTemplate.class)
    @ConditionalOnProperty(name = "telegram.bot.state-storage", havingValue = "redis", matchIfMissing = true)
    public static class RedisStorageConfiguration {
        @Bean
        @ConditionalOnMissingBean(StateRepository.class)
        public StateRepository redisStateRepository(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
            return new RedisStateRepository(redisTemplate, objectMapper);
        }

        @Bean
        @ConditionalOnMissingBean(HistoryRepository.class)
        public HistoryRepository redisHistoryRepository(StringRedisTemplate redisTemplate) {
            return new RedisHistoryRepository(redisTemplate);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(JPAStateRepository.class)
    @ConditionalOnProperty(name = "telegram.bot.state-storage", havingValue = "jpa")
    public static class JpaStorageConfiguration {
        @Bean
        @ConditionalOnMissingBean(StateRepository.class)
        public StateRepository jpaStateRepositoryAdapter(JPAStateRepository jpaStateRepository, ObjectMapper objectMapper) {
            return new StateRepositoryAdapter(jpaStateRepository, objectMapper);
        }

        @Bean
        @ConditionalOnMissingBean(HistoryRepository.class)
        public HistoryRepository jpaHistoryRepositoryAdapter(JPAHistoryRepository jpaHistoryRepository, ObjectMapper objectMapper) {
            return new HistoryRepositoryAdapter(jpaHistoryRepository, objectMapper);
        }
    }

    @Bean
    @ConditionalOnMissingBean(NavigationService.class)
    public NavigationService navigationService(HistoryRepository historyRepository) {
        return new NavigationService(historyRepository);
    }

    @Bean
    @ConditionalOnMissingBean(StateService.class)
    public StateService stateService(StateRepository stateRepository,
                                     UserInfoService userInfoService,
                                     NavigationService navigationService,
                                     @Autowired(required = false) List<StateHandler> stateHandlers) {
        return new StateService(
                stateRepository,
                userInfoService,
                navigationService,
                stateHandlers != null ? stateHandlers : Collections.emptyList()
        );
    }
}
