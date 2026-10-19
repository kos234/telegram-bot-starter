package kos.progs.telegram.bot.admin.repository;

import java.util.List;

public interface AdminsRepository {
    boolean isUserAdmin(long telegramId);
    void addUserToAdmins(long telegramId);
    void deleteUserFromAdmins(long telegramId);
    List<Long> getAdmins();
}
