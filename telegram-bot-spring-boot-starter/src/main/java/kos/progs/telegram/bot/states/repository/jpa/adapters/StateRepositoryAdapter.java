package kos.progs.telegram.bot.states.repository.jpa.adapters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kos.progs.telegram.bot.states.model.StateFrame;
import kos.progs.telegram.bot.states.repository.StateRepository;
import kos.progs.telegram.bot.states.repository.jpa.entity.StateEntity;
import kos.progs.telegram.bot.states.repository.jpa.repository.JPAStateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class StateRepositoryAdapter implements StateRepository {
    private final JPAStateRepository jpaStateRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void setChatState(long chatId, StateFrame frame) {
        if (frame == null) {
            jpaStateRepository.deleteById(chatId);
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(frame);
            jpaStateRepository.save(new StateEntity(chatId, json));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize state frame for chat {}", chatId, e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StateFrame> getChatState(long chatId) {
        return jpaStateRepository.findById(chatId)
                .flatMap(entity -> {
                    try {
                        return Optional.ofNullable(objectMapper.readValue(entity.getJsonState(), StateFrame.class));
                    } catch (JsonProcessingException e) {
                        log.error("Failed to deserialize state frame for chat {}", chatId, e);
                        return Optional.empty();
                    }
                });
    }
}
