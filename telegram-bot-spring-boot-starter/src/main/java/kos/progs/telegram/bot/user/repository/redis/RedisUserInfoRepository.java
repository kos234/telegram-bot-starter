package kos.progs.telegram.bot.user.repository.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kos.progs.telegram.bot.user.model.UserInfo;
import kos.progs.telegram.bot.user.repository.IUserInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class RedisUserInfoRepository implements IUserInfoRepository {
    private static final String KEY_PREFIX = "user:info:";
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<UserInfo> getUserInfo(long userId) {
        try {
            String json = redisTemplate.opsForValue().get(KEY_PREFIX + userId);
            if (json == null || json.isBlank()) {
                return Optional.empty();
            }
            return Optional.ofNullable(objectMapper.readValue(json, UserInfo.class));
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize UserInfo for user {}", userId, e);
            return Optional.empty();
        }
    }

    @Override
    public void setUserInfo(UserInfo userInfo) {
        if (userInfo == null) return;
        try {
            String json = objectMapper.writeValueAsString(userInfo);
            redisTemplate.opsForValue().set(KEY_PREFIX + userInfo.userId(), json);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize UserInfo for user {}", userInfo.userId(), e);
        }
    }
}
