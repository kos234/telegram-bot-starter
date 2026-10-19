package kos.progs.telegram.bot.payment.tinkoff.repository.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kos.progs.telegram.bot.payment.tinkoff.dto.TinkofURLDTO;
import kos.progs.telegram.bot.payment.tinkoff.repository.TinkoffRepository;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Optional;

public class RedisTinkoffRepository implements TinkoffRepository {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String KEY_PREFIX = "tinkoff:";

    public RedisTinkoffRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public RedisTinkoffRepository(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void savePaymentId(long orderId, TinkofURLDTO urls, ZonedDateTime expTime) {
        try {
            String json = objectMapper.writeValueAsString(urls);
            String redisKey = KEY_PREFIX + orderId;
            Duration ttl = Duration.between(ZonedDateTime.now(), expTime);
            if (ttl.isNegative() || ttl.isZero()) {
                ttl = Duration.ofMinutes(30);
            }
            redisTemplate.opsForValue().set(redisKey, json, ttl);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing TinkofURLDTO", e);
        }
    }

    @Override
    public Optional<TinkofURLDTO> getActiveUrl(long orderId) {
        String key = KEY_PREFIX + orderId;
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, TinkofURLDTO.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error deserializing TinkofURLDTO", e);
        }
    }
}
