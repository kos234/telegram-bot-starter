package kos.progs.telegram.bot.states.repository.memory;

import kos.progs.telegram.bot.states.repository.HistoryRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryHistoryRepository implements HistoryRepository {
    private final Map<Long, List<String>> storage = new ConcurrentHashMap<>();

    @Override
    public List<String> getHistory(long chatId) {
        return new ArrayList<>(storage.getOrDefault(chatId, Collections.emptyList()));
    }

    @Override
    public void setHistory(long chatId, List<String> history) {
        if (history == null || history.isEmpty()) {
            storage.remove(chatId);
        } else {
            storage.put(chatId, new ArrayList<>(history));
        }
    }

    @Override
    public void rollbackHistory(long chatId, int count) {
        List<String> list = storage.get(chatId);
        if (list == null || list.isEmpty()) return;
        int newSize = Math.max(0, list.size() - count);
        if (newSize == 0) {
            storage.remove(chatId);
        } else {
            storage.put(chatId, new ArrayList<>(list.subList(0, newSize)));
        }
    }

    @Override
    public void appendHistory(long chatId, String item) {
        if (item == null) return;
        storage.computeIfAbsent(chatId, k -> new ArrayList<>()).add(item);
    }

    @Override
    public String topHistory(long chatId) {
        List<String> list = storage.get(chatId);
        if (list == null || list.isEmpty()) return null;
        return list.get(list.size() - 1);
    }
}
