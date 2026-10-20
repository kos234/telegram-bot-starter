package kos.progs.telegram.bot.admin.button;

import kos.progs.telegram.bot.admin.presenter.TelegramAdminsPresenter;
import kos.progs.telegram.bot.button.handler.ButtonHandler;
import kos.progs.telegram.bot.core.model.HandleResult;
import kos.progs.telegram.bot.user.model.UserInfo;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.nio.ByteBuffer;

@RequiredArgsConstructor
public class AdminsViewListButton implements ButtonHandler {
    private final AdminsCommand adminsCommand;
    private final TelegramAdminsPresenter telegramAdminsPresenter;

    @Override
    public boolean isHandle(String command) {
        return adminsCommand.getViewListCommand().equals(command);
    }

    @Override
    public HandleResult handle(CallbackQuery callbackQuery, UserInfo userInfo, String command, ByteBuffer rawArgs) throws TelegramApiException {
        return telegramAdminsPresenter.present(userInfo.lang(), callbackQuery.getMessage().getChatId(), callbackQuery.getFrom().getId());
    }
}
