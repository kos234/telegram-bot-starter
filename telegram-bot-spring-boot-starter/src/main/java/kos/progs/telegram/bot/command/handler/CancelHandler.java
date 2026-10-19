package kos.progs.telegram.bot.command.handler;

import kos.progs.telegram.bot.button.handler.ButtonHandler;
import kos.progs.telegram.bot.common.model.Lang;
import kos.progs.telegram.bot.core.model.HandleResult;
import kos.progs.telegram.bot.core.service.BaseBotConsumer;
import kos.progs.telegram.bot.i18n.service.MessagesService;
import kos.progs.telegram.bot.states.model.HistoryAction;
import kos.progs.telegram.bot.states.model.MessageWithState;
import kos.progs.telegram.bot.states.service.NavigationService;
import kos.progs.telegram.bot.user.model.UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.MaybeInaccessibleMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.nio.ByteBuffer;

@Service
@RequiredArgsConstructor
@Slf4j
public class CancelHandler implements CommandHandler, ButtonHandler {
    private final NavigationService navigationService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final MessagesService messagesService;

    @Override
    public boolean isHandle(String command, String lastHistory) {
        return messagesService.getAll("cancel.title").contains(command);
    }

    @Override
    public boolean isHandle(String command) {
        return command.equals(messagesService.get("cancel.title", Lang.EN))
                || messagesService.getAll("cancel.title").contains(command);
    }

    @Override
    public HandleResult handle(CallbackQuery callbackQuery, UserInfo userInfo, String command, ByteBuffer rawArgs) throws TelegramApiException {
        realHandle(callbackQuery.getMessage().getChat(), callbackQuery.getFrom(), callbackQuery.getMessage());
        return HandleResult.success().history(HistoryAction.IGNORE).state(null).build();
    }

    public KeyboardButton getKeyboardButton(Lang lang) {
        return KeyboardButton.builder().text(messagesService.get("cancel.title", lang)).build();
    }

    public InlineKeyboardButton getInlineKeyboardButton(Lang lang) {
        return InlineKeyboardButton.builder()
                .callbackData(messagesService.get("cancel.title", Lang.EN))
                .text(messagesService.get("cancel.title", lang))
                .build();
    }

    @Override
    public HandleResult handle(Message message, UserInfo userInfo, String command) throws TelegramApiException {
        realHandle(message.getChat(), message.getFrom(), message);
        return HandleResult.success().history(HistoryAction.IGNORE).state(null).build();
    }

    private void realHandle(Chat chat, User from, MaybeInaccessibleMessage oldMessage) {
        String topCommand = navigationService.goBack(chat.getId(), 1);
        if (topCommand == null) {
            topCommand = "/start";
        }

        Update newUpdate = new Update();

        MessageWithState newMessage = new MessageWithState();
        newMessage.setMessageId(oldMessage.getMessageId());
        newMessage.setFrom(from);
        newMessage.setChat(chat);
        newMessage.setText(topCommand);
        newMessage.setJsonState(topCommand);

        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setMessage(newMessage);
        callbackQuery.setFrom(from);
        callbackQuery.setData(topCommand);
        callbackQuery.setId(null);

        newUpdate.setMessage(newMessage);
        newUpdate.setCallbackQuery(callbackQuery);

        applicationEventPublisher.publishEvent(new BaseBotConsumer.NewUpdateEvent(this, newUpdate));
    }
}
