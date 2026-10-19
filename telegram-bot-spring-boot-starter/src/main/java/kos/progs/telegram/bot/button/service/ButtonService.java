package kos.progs.telegram.bot.button.service;

import kos.progs.telegram.bot.button.handler.ButtonHandler;
import kos.progs.telegram.bot.button.model.ButtonArgs;
import kos.progs.telegram.bot.button.model.EmptyArgs;
import kos.progs.telegram.bot.core.model.HandleResult;
import kos.progs.telegram.bot.states.service.NavigationService;
import kos.progs.telegram.bot.states.service.StateService;
import kos.progs.telegram.bot.user.model.UserInfo;
import kos.progs.telegram.bot.user.service.UserInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ButtonService {
    private final List<ButtonHandler> buttonHandlers;
    private final UserInfoService userInfoService;
    private final StateService stateService;
    private final NavigationService navigationService;
    private final TelegramClient telegramClient;

    private static final Base64.Encoder ARGS_ENCODER = Base64.getEncoder().withoutPadding();
    private static final Base64.Decoder ARGS_DECODER = Base64.getDecoder();
    private static final ByteBuffer EMPTY_BUFFER = ByteBuffer.wrap(new byte[0]);

    public boolean handle(CallbackQuery callbackQuery) {
        if (callbackQuery == null) {
            return false;
        }

        UserInfo userInfo = userInfoService.getUserInfo(
                callbackQuery.getFrom().getId(),
                callbackQuery.getFrom().getLanguageCode()
        );

        String rawCommand = callbackQuery.getData();
        if (rawCommand == null) {
            return false;
        }

        String[] commandSplit = rawCommand.split("=");
        if (commandSplit.length == 0 || commandSplit.length > 2) {
            log.error("Unable to parse callback command: '{}'", rawCommand);
            return false;
        }

        ByteBuffer args = commandSplit.length == 2
                ? ByteBuffer.wrap(ARGS_DECODER.decode(commandSplit[1]))
                : EMPTY_BUFFER;

        HandleResult result = HandleResult.unhandled();
        for (ButtonHandler handler : buttonHandlers) {
            if (!handler.isHandle(commandSplit[0])) {
                continue;
            }
            try {
                args.rewind();
                result = handler.handle(callbackQuery, userInfo, commandSplit[0], args);
            } catch (TelegramApiException e) {
                log.error("Telegram API error in button handler for '{}'", commandSplit[0], e);
                result = HandleResult.error(e.getMessage());
            } catch (Exception e) {
                log.error("Unexpected error in button handler for '{}'", commandSplit[0], e);
                result = HandleResult.error(e.getMessage());
            }
            if (result != null && result.isHandle()) {
                break;
            }
        }

        try {
            if (callbackQuery.getId() != null) {
                telegramClient.execute(AnswerCallbackQuery.builder()
                        .callbackQueryId(callbackQuery.getId())
                        .build());
            }
        } catch (TelegramApiException e) {
            log.error("Error answering callback query: {}", e.getMessage());
        }

        if (result != null && result.isHandle()) {
            if (result.isSuccess()) {
                long chatId = callbackQuery.getMessage().getChatId();
                if (result.isUpdateState()) {
                    stateService.setChatState(chatId, result.getNewState());
                }
                navigationService.handleResult(
                        chatId,
                        result.getActionHistory(),
                        result.getHistory(rawCommand),
                        result.getRollbackCount()
                );
            }
            return true;
        }

        return false;
    }

    public static String buildCommand(String command) {
        return buildCommand(command, EmptyArgs.INSTANCE);
    }

    public static String buildCommand(String command, ButtonArgs args) {
        String argsString = args == null ? "" : ARGS_ENCODER.encodeToString(args.toByteBuffer().array());
        String endCommand = command + (argsString.isEmpty() ? "" : "=" + argsString);
        int byteLength = endCommand.getBytes(StandardCharsets.UTF_8).length;
        if (byteLength > 64) {
            throw new IllegalArgumentException("Callback command exceeds Telegram 64-byte limit: '" + endCommand + "'");
        }
        return endCommand;
    }
}
