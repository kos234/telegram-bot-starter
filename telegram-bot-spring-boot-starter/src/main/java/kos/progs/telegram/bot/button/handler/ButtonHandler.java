package kos.progs.telegram.bot.button.handler;

import kos.progs.telegram.bot.core.model.HandleResult;
import kos.progs.telegram.bot.user.model.UserInfo;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.nio.ByteBuffer;

public interface ButtonHandler {
    boolean isHandle(String command);
    HandleResult handle(CallbackQuery callbackQuery, UserInfo userInfo, String command, ByteBuffer rawArgs) throws TelegramApiException;
}
