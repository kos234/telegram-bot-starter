package kos.progs.telegram.bot.states.handler;

import kos.progs.telegram.bot.core.model.HandleResult;
import kos.progs.telegram.bot.states.model.StateFrame;
import kos.progs.telegram.bot.user.model.UserInfo;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface StateHandler {
    boolean isHandle(StateFrame frame);
    HandleResult handle(Message message, UserInfo userInfo, StateFrame frame) throws TelegramApiException;
}
