package kos.progs.telegram.bot.user.repository;

import kos.progs.telegram.bot.user.model.UserInfo;

import java.util.Optional;

public interface IUserInfoRepository {
    Optional<UserInfo> getUserInfo(long userId);
    void setUserInfo(UserInfo userInfo);
}
