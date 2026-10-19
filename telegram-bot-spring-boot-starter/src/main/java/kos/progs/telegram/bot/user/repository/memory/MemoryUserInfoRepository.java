package kos.progs.telegram.bot.user.repository.memory;

import kos.progs.telegram.bot.user.model.UserInfo;
import kos.progs.telegram.bot.user.repository.IUserInfoRepository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryUserInfoRepository implements IUserInfoRepository {
    private final Map<Long, UserInfo> storage = new ConcurrentHashMap<>();

    @Override
    public Optional<UserInfo> getUserInfo(long userId) {
        return Optional.ofNullable(storage.get(userId));
    }

    @Override
    public void setUserInfo(UserInfo userInfo) {
        if (userInfo != null) {
            storage.put(userInfo.userId(), userInfo);
        }
    }
}
