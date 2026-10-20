package kos.progs.telegram.bot.settings.button;

import kos.progs.telegram.bot.button.handler.ButtonHandler;
import kos.progs.telegram.bot.core.model.HandleResult;
import kos.progs.telegram.bot.i18n.service.MessagesService;
import kos.progs.telegram.bot.settings.model.UserInfoSetState;
import kos.progs.telegram.bot.user.model.UserInfo;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.nio.ByteBuffer;

@RequiredArgsConstructor
public class ChangeUserInfoButton implements ButtonHandler {
    private final MessagesService messagesService;
    private final ChangeUserInfoCommand changeUserInfoCommand;
    private final TelegramClient telegramClient;

    @Override
    public boolean isHandle(String command) {
        return changeUserInfoCommand.getPhoneChangeCommand().equals(command)
                || changeUserInfoCommand.getEmailChangeCommand().equals(command);
    }

    @Override
    public HandleResult handle(CallbackQuery callbackQuery, UserInfo userInfo, String command, ByteBuffer rawArgs) throws TelegramApiException {
        long chatId = callbackQuery.getMessage().getChatId();
        int changeState = -1;

        if (command.equals(changeUserInfoCommand.getEmailChangeCommand())) {
            telegramClient.execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(messagesService.get("settings.input_email", userInfo.lang()))
                    .build());
            changeState = UserInfoSetState.CHANGE_EMAIL;
        } else if (command.equals(changeUserInfoCommand.getPhoneChangeCommand())) {
            telegramClient.execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(messagesService.get("settings.input_phone", userInfo.lang()))
                    .build());
            changeState = UserInfoSetState.CHANGE_PHONE;
        }

        return HandleResult.success()
                .state(changeState == -1 ? null : new UserInfoSetState(changeState))
                .build();
    }
}
