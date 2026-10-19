package kos.progs.telegram.bot.command.service;

import kos.progs.telegram.bot.command.handler.CommandHandler;
import kos.progs.telegram.bot.core.model.HandleResult;
import kos.progs.telegram.bot.states.service.NavigationService;
import kos.progs.telegram.bot.states.service.StateService;
import kos.progs.telegram.bot.user.model.UserInfo;
import kos.progs.telegram.bot.user.service.UserInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommandService {
    private final List<CommandHandler> commandHandlers;
    private final StateService stateService;
    private final NavigationService navigationService;
    private final UserInfoService userInfoService;

    public boolean handle(Message message) {
        if (message == null || message.getText() == null) {
            return false;
        }
        long chatId = message.getChatId();

        UserInfo userInfo = userInfoService.getUserInfo(
                message.getFrom().getId(),
                message.getFrom().getLanguageCode()
        );
        String command = message.getText();
        String history = navigationService.peakHistory(chatId);

        HandleResult handleResult = HandleResult.unhandled();
        for (CommandHandler handler : commandHandlers) {
            if (!handler.isHandle(command, history)) {
                continue;
            }
            try {
                handleResult = handler.handle(message, userInfo, command);
            } catch (TelegramApiException e) {
                log.error("Telegram API error handling command '{}'", command, e);
                handleResult = HandleResult.error(e.getMessage());
            } catch (Exception e) {
                log.error("Unexpected error handling command '{}'", command, e);
                handleResult = HandleResult.error(e.getMessage());
            }
            if (handleResult.isHandle()) {
                break;
            }
        }

        if (handleResult.isHandle()) {
            if (handleResult.isSuccess()) {
                navigationService.handleResult(
                        chatId,
                        handleResult.getActionHistory(),
                        handleResult.getHistory(command),
                        handleResult.getRollbackCount()
                );

                if (handleResult.isUpdateState()) {
                    stateService.setChatState(message.getChatId(), handleResult.getNewState());
                }
            }
            return true;
        }

        return false;
    }
}
