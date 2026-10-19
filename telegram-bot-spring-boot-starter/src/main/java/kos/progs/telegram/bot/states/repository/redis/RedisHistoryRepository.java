package kos.progs.telegram.bot.states.repository.redis;

import kos.progs.telegram.bot.states.repository.HistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class RedisHistoryRepository implements HistoryRepository {
    private static final String KEY_PREFIX = "chat:history:";
    private final StringRedisTemplate redisTemplate;

    @Override
    public List<String> getHistory(long chatId) {
        List<String> list = redisTemplate.opsForList().range(KEY_PREFIX + chatId, 0, -1);
        return list != null ? list : Collections.emptyList();
    }

    @Override
    public void setHistory(long chatId, List<String> history) {
        String key = KEY_PREFIX + chatId;
        redisTemplate.delete(key);
        if (history != null && !history.isEmpty()) {
            redisTemplate.opsForList().rightPushAll(key, history);
        }
    }

    @Override
    public void rollbackHistory(long chatId, int count) {
        String key = KEY_PREFIX + chatId;
        for (int i = 0; i < count; i++) {
            redisTemplate.opsForList().rightPop(key);
        }
    }

    @Override
    public void appendHistory(long chatId, String item) {
        if (item == null) return;
        redisTemplate.opsForList().rightPush(KEY_PREFIX + chatId, item);
    }

    @Override
    public String topHistory(long chatId) {
        List<String> last = redisTemplate.opsForList().range(KEY_PREFIX + chatId, -1, -1);
        if (last == null || last.isEmpty()) return null;
        return last.get(0);
    }
}
