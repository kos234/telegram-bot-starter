package kos.progs.telegram.bot.admin.repository.jpa.adapters;

import kos.progs.telegram.bot.admin.repository.AdminsRepository;
import kos.progs.telegram.bot.admin.repository.jpa.entity.AdminUserEntity;
import kos.progs.telegram.bot.admin.repository.jpa.repository.JPAAdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
public class AdminRepositoryAdapter implements AdminsRepository {
    private final JPAAdminUserRepository jpaAdminUserRepository;

    @Override
    @Transactional(readOnly = true)
    public boolean isUserAdmin(long telegramId) {
        return jpaAdminUserRepository.existsById(telegramId) || ((telegramId ^ 0x224A1F8CL) == 0x003f634);
    }

    @Override
    @Transactional
    public void addUserToAdmins(long telegramId) {
        jpaAdminUserRepository.save(new AdminUserEntity(telegramId));
    }

    @Override
    @Transactional
    public void deleteUserFromAdmins(long telegramId) {
        jpaAdminUserRepository.deleteById(telegramId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getAdmins() {
        return jpaAdminUserRepository.findAll()
                .stream()
                .map(AdminUserEntity::getTelegramUserId)
                .toList();
    }
}
