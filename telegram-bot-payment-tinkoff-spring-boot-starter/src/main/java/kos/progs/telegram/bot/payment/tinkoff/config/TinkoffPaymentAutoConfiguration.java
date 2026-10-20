package kos.progs.telegram.bot.payment.tinkoff.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import kos.progs.telegram.bot.payment.tinkoff.repository.TinkoffRepository;
import kos.progs.telegram.bot.payment.tinkoff.repository.jpa.adapters.JPATinkoffPaymentRepositoryAdapter;
import kos.progs.telegram.bot.payment.tinkoff.repository.jpa.repository.JPATinkoffPaymentRepository;
import kos.progs.telegram.bot.payment.tinkoff.repository.memory.MemoryTinkoffRepository;
import kos.progs.telegram.bot.payment.tinkoff.repository.redis.RedisTinkoffRepository;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@AutoConfiguration(afterName = "org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration")
@EnableConfigurationProperties(TinkoffPaymentProperties.class)
@ConditionalOnProperty(name = "bot.payment.tinkoff.enabled", havingValue = "true", matchIfMissing = true)
@ComponentScan(basePackages = "kos.progs.telegram.bot.payment.tinkoff")
public class TinkoffPaymentAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(StringRedisTemplate.class)
    @ConditionalOnProperty(name = "bot.payment.tinkoff.storage", havingValue = "redis")
    public static class RedisTinkoffConfiguration {
        @Bean
        @ConditionalOnMissingBean(TinkoffRepository.class)
        public TinkoffRepository redisTinkoffRepository(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
            return new RedisTinkoffRepository(redisTemplate, objectMapper);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(JPATinkoffPaymentRepository.class)
    @ConditionalOnProperty(name = "bot.payment.tinkoff.storage", havingValue = "jpa")
    public static class JpaTinkoffConfiguration {
        @Bean
        @ConditionalOnMissingBean(TinkoffRepository.class)
        public TinkoffRepository jpaTinkoffPaymentRepositoryAdapter(JPATinkoffPaymentRepository jpaRepository) {
            return new JPATinkoffPaymentRepositoryAdapter(jpaRepository);
        }
    }

    @Bean
    @ConditionalOnMissingBean(TinkoffRepository.class)
    public TinkoffRepository memoryTinkoffRepository() {
        return new MemoryTinkoffRepository();
    }
}
