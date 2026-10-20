package kos.progs.telegram.bot.states.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kos.progs.telegram.bot.core.model.HandleResult;
import kos.progs.telegram.bot.states.handler.StateHandler;
import kos.progs.telegram.bot.states.model.MessageWithState;
import kos.progs.telegram.bot.states.model.StateFrame;
import kos.progs.telegram.bot.states.repository.StateRepository;
import kos.progs.telegram.bot.user.model.UserInfo;
import kos.progs.telegram.bot.user.service.UserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class StateService {
    private final StateRepository stateRepository;
    private final UserInfoService userInfoService;
    private final NavigationService navigationService;
    private final List<StateHandler> stateHandlers;
    private final ObjectMapper objectMapper;

    public StateService(StateRepository stateRepository,
                        UserInfoService userInfoService,
                        NavigationService navigationService,
                        List<StateHandler> stateHandlers) {
        this.stateRepository = stateRepository;
        this.userInfoService = userInfoService;
        this.navigationService = navigationService;
        this.stateHandlers = stateHandlers;
        this.objectMapper = new ObjectMapper();
    }

    public Optional<StateFrame> getChatState(long chatId) {
        return stateRepository.getChatState(chatId);
    }

    public void setChatState(long chatId, StateFrame frame) {
        stateRepository.setChatState(chatId, frame);
    }

    public boolean handle(Message message) {
        if (message == null || message.getFrom() == null) {
            return false;
        }

        UserInfo userInfo = userInfoService.getUserInfo(
                message.getFrom().getId(),
                message.getFrom().getLanguageCode()
        );

        Optional<StateFrame> frameWrapper = (message instanceof MessageWithState msg)
                ? tryParseState(msg.getJsonState())
                : Optional.empty();
        frameWrapper = frameWrapper.or(() -> getChatState(message.getChatId()));

        if (frameWrapper.isEmpty()) {
            return false;
        }

        StateFrame frame = frameWrapper.get();
        HandleResult handleResult = HandleResult.unhandled();
        for (StateHandler handler : stateHandlers) {
            if (!handler.isHandle(frame)) {
                continue;
            }

            try {
                handleResult = handler.handle(message, userInfo, frame);
            } catch (TelegramApiException e) {
                log.error("Telegram API error handling state '{}'", frame.getClass().getSimpleName(), e);
                handleResult = HandleResult.error(e.getMessage());
            } catch (Exception e) {
                log.error("Unexpected error handling state '{}'", frame.getClass().getSimpleName(), e);
                handleResult = HandleResult.error(e.getMessage());
            }
            if (handleResult.isHandle()) {
                break;
            }
        }

        if (handleResult.isHandle()) {
            if (handleResult.isSuccess()) {
                navigationService.handleResult(
                        message.getChatId(),
                        handleResult.getActionHistory(),
                        handleResult.getHistory(stateToJson(frame)),
                        handleResult.getRollbackCount()
                );
                if (handleResult.isUpdateState()) {
                    setChatState(message.getChatId(), handleResult.getNewState());
                }
            }
            return true;
        }

        return false;
    }

    private Optional<StateFrame> tryParseState(String jsonState) {
        if (jsonState == null || jsonState.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(jsonState, StateFrame.class));
        } catch (JsonProcessingException e) {
            return Optional.empty();
        }
    }

    private String stateToJson(StateFrame frame) {
        try {
            return objectMapper.writeValueAsString(frame);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting StateFrame to JSON", e);
        }
    }
}
