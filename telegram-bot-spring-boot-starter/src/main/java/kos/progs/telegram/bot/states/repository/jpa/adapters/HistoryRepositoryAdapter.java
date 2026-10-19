package kos.progs.telegram.bot.states.repository.jpa.adapters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kos.progs.telegram.bot.states.repository.HistoryRepository;
import kos.progs.telegram.bot.states.repository.jpa.entity.HistoryEntity;
import kos.progs.telegram.bot.states.repository.jpa.repository.JPAHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class HistoryRepositoryAdapter implements HistoryRepository {
    private final JPAHistoryRepository jpaHistoryRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public List<String> getHistory(long chatId) {
        return jpaHistoryRepository.findById(chatId)
                .map(entity -> {
                    try {
                        return objectMapper.readValue(entity.getJsonHistory(), new TypeReference<List<String>>() {});
                    } catch (JsonProcessingException e) {
                        log.error("Failed to deserialize history for chat {}", chatId, e);
                        return Collections.<String>emptyList();
                    }
                })
                .orElse(Collections.emptyList());
    }

    @Override
    @Transactional
    public void setHistory(long chatId, List<String> history) {
        if (history == null || history.isEmpty()) {
            jpaHistoryRepository.deleteById(chatId);
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(history);
            jpaHistoryRepository.save(new HistoryEntity(chatId, json));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize history for chat {}", chatId, e);
        }
    }

    @Override
    @Transactional
    public void rollbackHistory(long chatId, int count) {
        List<String> list = new ArrayList<>(getHistory(chatId));
        if (list.isEmpty()) return;
        int newSize = Math.max(0, list.size() - count);
        setHistory(chatId, list.subList(0, newSize));
    }

    @Override
    @Transactional
    public void appendHistory(long chatId, String item) {
        if (item == null) return;
        List<String> list = new ArrayList<>(getHistory(chatId));
        list.add(item);
        setHistory(chatId, list);
    }

    @Override
    @Transactional(readOnly = true)
    public String topHistory(long chatId) {
        List<String> list = getHistory(chatId);
        if (list.isEmpty()) return null;
        return list.get(list.size() - 1);
    }
}
