package kos.progs.telegram.bot.user.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JPAUserInfoRepository extends JpaRepository<UserInfoEntity, Long> {
}
