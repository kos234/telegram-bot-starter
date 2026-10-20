package kos.progs.telegram.bot.settings.state;

import kos.progs.telegram.bot.core.model.HandleResult;
import kos.progs.telegram.bot.core.service.BaseBotConsumer;
import kos.progs.telegram.bot.i18n.service.MessagesService;
import kos.progs.telegram.bot.settings.model.UserInfoSetState;
import kos.progs.telegram.bot.states.handler.StateHandler;
import kos.progs.telegram.bot.states.model.HistoryAction;
import kos.progs.telegram.bot.states.model.StateFrame;
import kos.progs.telegram.bot.user.model.UserInfo;
import kos.progs.telegram.bot.user.service.UserInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@RequiredArgsConstructor
public class UserInfoChangeStateHandler implements StateHandler {
    private final UserInfoService userInfoService;
    private final MessagesService messagesService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final TelegramClient telegramClient;

    @Override
    public boolean isHandle(StateFrame frame) {
        return frame instanceof UserInfoSetState;
    }

    @Override
    public HandleResult handle(Message message, UserInfo userInfo, StateFrame frame) throws TelegramApiException {
        UserInfoSetState state = (UserInfoSetState) frame;
        String input = message.getText();
        long userId = message.getFrom().getId();
        long chatId = message.getChatId();

        UserInfo newUserInfo = null;

        if (state.getChange() == UserInfoSetState.CHANGE_EMAIL) {
            newUserInfo = userInfoService.updateEmail(userId, input);
            if (newUserInfo == null) {
                telegramClient.execute(SendMessage.builder()
                        .chatId(chatId)
                        .text(messagesService.get("settings.input_email_bad", userInfo.lang()))
                        .build());
                return HandleResult.error("Invalid email");
            }
        } else if (state.getChange() == UserInfoSetState.CHANGE_PHONE) {
            newUserInfo = userInfoService.updatePhone(userId, input);
            if (newUserInfo == null) {
                telegramClient.execute(SendMessage.builder()
                        .chatId(chatId)
                        .text(messagesService.get("settings.input_phone_bad", userInfo.lang()))
                        .build());
                return HandleResult.error("Invalid phone");
            }
        }

        if (newUserInfo != null) {
            telegramClient.execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(messagesService.get("settings.updated", userInfo.lang()))
                    .build());

            Update newUpdate = new Update();
            newUpdate.setMessage(Message.builder()
                    .from(message.getFrom())
                    .chat(message.getChat())
                    .text(messagesService.get("settings.title", userInfo.lang()))
                    .build());

            applicationEventPublisher.publishEvent(new BaseBotConsumer.NewUpdateEvent(this, newUpdate));
        }

        return HandleResult.success().history(HistoryAction.ROLLBACK).state(null).build();
    }
}
