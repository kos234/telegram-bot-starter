package kos.progs.telegram.bot.user.repository.jpa.adapters;

import kos.progs.telegram.bot.user.model.UserInfo;
import kos.progs.telegram.bot.user.repository.IUserInfoRepository;
import kos.progs.telegram.bot.user.repository.jpa.mappers.UserInfoMapper;
import kos.progs.telegram.bot.user.repository.jpa.repository.JPAUserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
public class UserInfoRepositoryAdapter implements IUserInfoRepository {
    private final JPAUserInfoRepository jpaUserInfoRepository;
    private final UserInfoMapper userInfoMapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<UserInfo> getUserInfo(long userId) {
        return jpaUserInfoRepository.findById(userId).map(userInfoMapper::toModel);
    }

    @Override
    @Transactional
    public void setUserInfo(UserInfo userInfo) {
        if (userInfo != null) {
            jpaUserInfoRepository.save(userInfoMapper.toEntity(userInfo));
        }
    }
}
