package kos.progs.telegram.bot.lists.states;

import kos.progs.telegram.bot.common.model.Currency;
import kos.progs.telegram.bot.common.model.Lang;
import kos.progs.telegram.bot.core.model.HandleResult;
import kos.progs.telegram.bot.i18n.service.MessagesService;
import kos.progs.telegram.bot.lists.model.AbstractMoveToArgs;
import kos.progs.telegram.bot.states.handler.StateHandler;
import kos.progs.telegram.bot.states.model.HistoryAction;
import kos.progs.telegram.bot.states.model.StateFrame;
import kos.progs.telegram.bot.user.model.UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.nio.ByteBuffer;

@RequiredArgsConstructor
@Slf4j
public abstract class AbstractGoToPageHandler implements StateHandler {
    private final MessagesService messagesService;
    private final TelegramClient telegramClient;

    @Override
    public boolean isHandle(StateFrame frame) {
        return frame instanceof GoToPageState goToPageState && isHandle(goToPageState.getPrefix());
    }

    protected abstract boolean isHandle(String prefix);

    @Override
    public HandleResult handle(Message message, UserInfo userInfo, StateFrame frame) throws TelegramApiException {
        var goToPageState = (GoToPageState) frame;
        var args = factoryByArgs();
        args.fromByteBuffer(ByteBuffer.wrap(goToPageState.getArgs()));
        long userId = message.getFrom().getId();
        long chatId = message.getChatId();

        String text = message.getText();
        if (text == null || text.isBlank()) {
            sendError(chatId, messagesService.get("global.error.empty_value", userInfo.lang()));
            return HandleResult.error("Empty page value");
        }

        int page;
        try {
            page = Integer.parseInt(text.trim());
        } catch (NumberFormatException numberFormatException) {
            sendError(chatId, messagesService.get("global.error.value_not_number", userInfo.lang()));
            return HandleResult.error("Page value is not a number");
        }

        if (page < 1) {
            sendError(chatId, messagesService.get("global.error.value_less", userInfo.lang(), 1));
            return HandleResult.error("Page value less than 1");
        }

        int maxPage = getMaxPage(userId, args);
        if (page > maxPage) {
            sendError(chatId, messagesService.get("global.error.value_great", userInfo.lang(), maxPage));
            return HandleResult.error("Page value greater than maxPage");
        }

        return moveList(userInfo.lang(), userInfo.currency(), chatId, userId, page - 1, goToPageState.getMessageId(), args)
                .withActionHistory(HistoryAction.ROLLBACK)
                .withUpdateState(true);
    }

    private void sendError(long chatId, String message) throws TelegramApiException {
        telegramClient.execute(SendMessage.builder()
                .chatId(chatId)
                .text(message)
                .build());
    }

    public abstract AbstractMoveToArgs factoryByArgs();
    public abstract HandleResult moveList(Lang lang, Currency currency, long chatId, long userId, int index, int messageId, AbstractMoveToArgs args) throws TelegramApiException;
    public abstract int getMaxPage(long userId, AbstractMoveToArgs args);
}
