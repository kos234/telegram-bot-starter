package kos.progs.telegram.bot.user.repository.jpa.repository;

import kos.progs.telegram.bot.user.repository.jpa.entity.UserInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JPAUserInfoRepository extends JpaRepository<UserInfoEntity, Long> {
}
