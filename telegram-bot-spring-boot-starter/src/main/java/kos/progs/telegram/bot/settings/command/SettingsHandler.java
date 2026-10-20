package kos.progs.telegram.bot.settings.command;

import kos.progs.telegram.bot.button.handler.ButtonHandler;
import kos.progs.telegram.bot.command.handler.CommandHandler;
import kos.progs.telegram.bot.core.model.HandleResult;
import kos.progs.telegram.bot.i18n.service.MessagesService;
import kos.progs.telegram.bot.settings.presenter.SettingsPresenter;
import kos.progs.telegram.bot.user.model.UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.nio.ByteBuffer;

@RequiredArgsConstructor
@Slf4j
public class SettingsHandler implements CommandHandler, ButtonHandler {
    private final SettingsPresenter settingsPresenter;
    private final MessagesService messagesService;

    @Override
    public boolean isHandle(String command, String lastHandler) {
        return messagesService.getAll("settings.title").contains(command);
    }

    @Override
    public HandleResult handle(Message message, UserInfo userInfo, String command) throws TelegramApiException {
        return settingsPresenter.presentSettings(userInfo, message.getChatId());
    }

    @Override
    public boolean isHandle(String command) {
        return messagesService.getAll("settings.title").contains(command);
    }

    @Override
    public HandleResult handle(CallbackQuery callbackQuery, UserInfo userInfo, String command, ByteBuffer rawArgs) throws TelegramApiException {
        return settingsPresenter.presentSettings(userInfo, callbackQuery.getMessage().getChatId());
    }
}
