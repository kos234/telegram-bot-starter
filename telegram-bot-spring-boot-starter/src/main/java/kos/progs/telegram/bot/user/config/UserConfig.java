package kos.progs.telegram.bot.user.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import kos.progs.telegram.bot.user.repository.IUserInfoRepository;
import kos.progs.telegram.bot.user.repository.jpa.UserInfoRepositoryAdapter;
import kos.progs.telegram.bot.user.repository.jpa.UserInfoMapper;
import kos.progs.telegram.bot.user.repository.jpa.JPAUserInfoRepository;
import kos.progs.telegram.bot.user.repository.memory.MemoryUserInfoRepository;
import kos.progs.telegram.bot.user.repository.redis.RedisUserInfoRepository;
import kos.progs.telegram.bot.user.service.UserInfoService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class UserConfig {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(name = "telegram.bot.user-storage", havingValue = "memory", matchIfMissing = true)
    public static class MemoryUserStorageConfiguration {
        @Bean
        @ConditionalOnMissingBean(IUserInfoRepository.class)
        public IUserInfoRepository memoryUserInfoRepository() {
            return new MemoryUserInfoRepository();
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(StringRedisTemplate.class)
    @ConditionalOnProperty(name = "telegram.bot.user-storage", havingValue = "redis")
    public static class RedisUserStorageConfiguration {
        @Bean
        @ConditionalOnMissingBean(IUserInfoRepository.class)
        public IUserInfoRepository redisUserInfoRepository(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
            return new RedisUserInfoRepository(redisTemplate, objectMapper);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(JPAUserInfoRepository.class)
    @ConditionalOnProperty(name = "telegram.bot.user-storage", havingValue = "jpa")
    public static class JpaUserStorageConfiguration {
        @Bean
        @ConditionalOnMissingBean
        public UserInfoMapper userInfoMapper() {
            return new UserInfoMapper();
        }

        @Bean
        @ConditionalOnBean(JPAUserInfoRepository.class)
        @ConditionalOnMissingBean(IUserInfoRepository.class)
        public IUserInfoRepository jpaUserInfoRepositoryAdapter(JPAUserInfoRepository jpaUserInfoRepository,
                                                                UserInfoMapper userInfoMapper) {
            return new UserInfoRepositoryAdapter(jpaUserInfoRepository, userInfoMapper);
        }
    }

    @Bean
    @ConditionalOnMissingBean(UserInfoService.class)
    public UserInfoService userInfoService(IUserInfoRepository userInfoRepository) {
        return new UserInfoService(userInfoRepository);
    }
}
