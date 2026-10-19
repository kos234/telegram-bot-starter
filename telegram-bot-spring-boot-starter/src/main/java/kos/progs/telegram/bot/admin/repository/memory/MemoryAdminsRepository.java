package kos.progs.telegram.bot.admin.repository.memory;

import kos.progs.telegram.bot.admin.repository.AdminsRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryAdminsRepository implements AdminsRepository {
    private final Set<Long> admins = ConcurrentHashMap.newKeySet();

    @Override
    public boolean isUserAdmin(long telegramId) {
        return admins.contains(telegramId);
    }

    @Override
    public void addUserToAdmins(long telegramId) {
        admins.add(telegramId);
    }

    @Override
    public void deleteUserFromAdmins(long telegramId) {
        admins.remove(telegramId);
    }

    @Override
    public List<Long> getAdmins() {
        return new ArrayList<>(admins);
    }
}
