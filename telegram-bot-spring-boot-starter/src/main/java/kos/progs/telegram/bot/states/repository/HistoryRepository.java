package kos.progs.telegram.bot.states.repository;

import java.util.List;

public interface HistoryRepository {
    List<String> getHistory(long chatId);
    void setHistory(long chatId, List<String> history);
    void rollbackHistory(long chatId, int count);
    void appendHistory(long chatId, String item);
    String topHistory(long chatId);
}
