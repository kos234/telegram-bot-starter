package kos.progs.telegram.bot.states.repository.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kos.progs.telegram.bot.states.model.StateFrame;
import kos.progs.telegram.bot.states.repository.StateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class RedisStateRepository implements StateRepository {
    private static final String KEY_PREFIX = "chat:state:";
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<StateFrame> getChatState(long chatId) {
        try {
            String json = redisTemplate.opsForValue().get(KEY_PREFIX + chatId);
            if (json == null || json.isBlank()) {
                return Optional.empty();
            }
            return Optional.ofNullable(objectMapper.readValue(json, StateFrame.class));
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize state frame for chat {}", chatId, e);
            return Optional.empty();
        }
    }

    @Override
    public void setChatState(long chatId, StateFrame frame) {
        if (frame == null) {
            redisTemplate.delete(KEY_PREFIX + chatId);
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(frame);
            redisTemplate.opsForValue().set(KEY_PREFIX + chatId, json);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize state frame for chat {}", chatId, e);
        }
    }
}
