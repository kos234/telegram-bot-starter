package kos.progs.telegram.bot.states.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import kos.progs.telegram.bot.states.model.HistoryAction;
import kos.progs.telegram.bot.states.repository.HistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class NavigationService {
    private final HistoryRepository historyRepository;
    private final Cache<Long, String> historyCache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();

    public void handleResult(long chatId, HistoryAction historyAction, String history, int rollbackCount) {
        if (historyAction == null) return;
        switch (historyAction) {
            case ADD -> navigateTo(chatId, Objects.requireNonNull(history));
            case ROLLBACK -> goBack(chatId, rollbackCount);
            case IGNORE -> { /* do nothing */ }
        }
    }

    private void logHistory(long chatId) {
        if (log.isDebugEnabled()) {
            log.debug("History for chatId {}: {}", chatId, String.join(", ", historyRepository.getHistory(chatId)));
        }
    }

    private void pushHistory(long chatId, String command) {
        historyRepository.appendHistory(chatId, command);
        historyCache.put(chatId, command);
    }

    private void rollbackHistory(long chatId, int count) {
        historyRepository.rollbackHistory(chatId, count);
        historyCache.invalidate(chatId);
    }

    public String peakHistory(long chatId) {
        return historyCache.get(chatId, historyRepository::topHistory);
    }

    public void navigateTo(long chatId, String command) {
        var currentItem = peakHistory(chatId);
        if (currentItem != null && currentItem.equals(command)) {
            return;
        }
        pushHistory(chatId, command);
        logHistory(chatId);
    }

    public String goBack(long chatId, int count) {
        if (count < 1) {
            throw new IllegalArgumentException("Rollback count must be at least 1: count=" + count);
        }
        rollbackHistory(chatId, count);
        var currentItem = peakHistory(chatId);
        logHistory(chatId);
        return currentItem == null ? "/start" : currentItem;
    }

    public void resetHistory(long chatId) {
        historyRepository.setHistory(chatId, List.of());
        historyCache.invalidate(chatId);
    }
}
