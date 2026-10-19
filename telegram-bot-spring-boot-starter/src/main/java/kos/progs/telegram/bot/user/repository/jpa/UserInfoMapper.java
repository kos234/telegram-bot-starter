package kos.progs.telegram.bot.user.repository.jpa;

import kos.progs.telegram.bot.user.model.UserInfo;

public class UserInfoMapper {
    public UserInfo toModel(UserInfoEntity entity) {
        if (entity == null) return null;
        return new UserInfo(
                entity.getUserId(),
                entity.getUserLang(),
                entity.getCurrency(),
                entity.getEmail(),
                entity.getPhone()
        );
    }

    public UserInfoEntity toEntity(UserInfo userInfo) {
        if (userInfo == null) return null;
        return new UserInfoEntity(
                userInfo.userId(),
                userInfo.lang(),
                userInfo.currency(),
                userInfo.email(),
                userInfo.phone()
        );
    }
}
