package kos.progs.telegram.bot.admin.repository.jpa.repository;

import kos.progs.telegram.bot.admin.repository.jpa.entity.AdminUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JPAAdminUserRepository extends JpaRepository<AdminUserEntity, Long> {
}
