package kos.progs.telegram.bot.command.handler;

import kos.progs.telegram.bot.core.model.HandleResult;
import kos.progs.telegram.bot.user.model.UserInfo;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface CommandHandler {
    boolean isHandle(String command, String lastHistory);
    HandleResult handle(Message message, UserInfo userInfo, String command) throws TelegramApiException;
}
