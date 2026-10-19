package kos.progs.telegram.bot.states.repository.memory;

import kos.progs.telegram.bot.states.model.StateFrame;
import kos.progs.telegram.bot.states.repository.StateRepository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryStateRepository implements StateRepository {
    private final Map<Long, StateFrame> storage = new ConcurrentHashMap<>();

    @Override
    public Optional<StateFrame> getChatState(long chatId) {
        return Optional.ofNullable(storage.get(chatId));
    }

    @Override
    public void setChatState(long chatId, StateFrame frame) {
        if (frame == null) {
            storage.remove(chatId);
        } else {
            storage.put(chatId, frame);
        }
    }
}
