package kos.progs.telegram.bot.lists.button;

import kos.progs.telegram.bot.button.handler.ButtonHandler;
import kos.progs.telegram.bot.command.handler.CancelHandler;
import kos.progs.telegram.bot.common.model.Currency;
import kos.progs.telegram.bot.common.model.Lang;
import kos.progs.telegram.bot.core.model.HandleResult;
import kos.progs.telegram.bot.i18n.service.MessagesService;
import kos.progs.telegram.bot.lists.model.AbstractMoveArgs;
import kos.progs.telegram.bot.lists.model.AbstractMoveToArgs;
import kos.progs.telegram.bot.lists.states.GoToPageState;
import kos.progs.telegram.bot.states.model.HistoryAction;
import kos.progs.telegram.bot.states.model.StateFrame;
import kos.progs.telegram.bot.user.model.UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.nio.ByteBuffer;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractMoveButtons implements ButtonHandler {
    protected final MessagesService messagesService;
    protected final TelegramClient telegramClient;
    protected final CancelHandler cancelHandler;
    protected final AbstractMoveCommand aCommand;

    @Override
    public boolean isHandle(String command) {
        return aCommand.getNextCommand().equals(command)
                || aCommand.getByCommand().equals(command)
                || aCommand.getPrevCommand().equals(command);
    }

    @Override
    public HandleResult handle(CallbackQuery callbackQuery, UserInfo userInfo, String command, ByteBuffer rawArgs) throws TelegramApiException {
        int messageId = callbackQuery.getMessage().getMessageId();
        long chatId = callbackQuery.getMessage().getChatId();
        long userId = callbackQuery.getFrom().getId();

        if (command.equals(aCommand.getByCommand())) {
            var args = factoryByArgs();
            args.fromByteBuffer(rawArgs);
            return realHandleByIndex(userInfo.lang(), chatId, messageId, args);
        }

        var args = factoryArgs();
        args.fromByteBuffer(rawArgs);

        int nextIndex;
        if (command.equals(aCommand.getNextCommand())) {
            nextIndex = args.getIndex() + 1;
        } else if (command.equals(aCommand.getPrevCommand())) {
            nextIndex = args.getIndex() - 1;
        } else {
            log.error("Unknown move command: {}", command);
            return HandleResult.unhandled();
        }

        if (nextIndex < 0) {
            return HandleResult.unhandled();
        }

        return moveList(userInfo.lang(), userInfo.currency(), chatId, userId, nextIndex, messageId, args);
    }

    protected HandleResult realHandleByIndex(Lang lang, long chatId, int messageId, AbstractMoveToArgs args) throws TelegramApiException {
        var replyKeyboardMarkup = new ReplyKeyboardMarkup(List.of(
                new KeyboardRow(cancelHandler.getKeyboardButton(lang))
        ));
        replyKeyboardMarkup.setResizeKeyboard(true);

        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(messagesService.get("page.select_page", lang))
                .replyMarkup(replyKeyboardMarkup)
                .build();
        telegramClient.execute(sendMessage);

        return HandleResult.success()
                .history(HistoryAction.ADD, aCommand.getPrefix() + "GO_TO_PAGE")
                .state(getGotoPageState(messageId, args))
                .build();
    }

    public abstract AbstractMoveArgs factoryArgs();
    public abstract AbstractMoveToArgs factoryByArgs();
    public abstract HandleResult moveList(Lang lang, Currency currency, long chatId, long userId, int index, int messageId, AbstractMoveArgs args) throws TelegramApiException;

    public StateFrame getGotoPageState(int messageId, AbstractMoveToArgs args) {
        return GoToPageState.builder()
                .messageId(messageId)
                .prefix(aCommand.getPrefix())
                .args(args.toByteBuffer().array())
                .build();
    }
}
