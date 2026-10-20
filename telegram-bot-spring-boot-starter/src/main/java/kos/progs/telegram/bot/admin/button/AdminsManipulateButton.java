package kos.progs.telegram.bot.admin.button;

import kos.progs.telegram.bot.admin.model.AdminsManipulateFrame;
import kos.progs.telegram.bot.admin.service.AdminService;
import kos.progs.telegram.bot.button.handler.ButtonHandler;
import kos.progs.telegram.bot.core.model.HandleResult;
import kos.progs.telegram.bot.i18n.service.MessagesService;
import kos.progs.telegram.bot.user.model.UserInfo;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.nio.ByteBuffer;

@RequiredArgsConstructor
public class AdminsManipulateButton implements ButtonHandler {
    private final AdminsCommand adminsCommand;
    private final MessagesService messagesService;
    private final AdminService adminService;
    private final TelegramClient telegramClient;

    @Override
    public boolean isHandle(String command) {
        return adminsCommand.getAddAdminCommand().equals(command)
                || adminsCommand.getRemoveAdminCommand().equals(command);
    }

    @Override
    public HandleResult handle(CallbackQuery callbackQuery, UserInfo userInfo, String command, ByteBuffer rawArgs) throws TelegramApiException {
        if (!adminService.isUserAdmin(callbackQuery.getFrom().getId())) {
            return HandleResult.unhandled();
        }

        SendMessage sendMessage = SendMessage.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .text(messagesService.get("admin.order.settings.user_input", userInfo.lang()))
                .build();
        telegramClient.execute(sendMessage);

        boolean isAdd = adminsCommand.getAddAdminCommand().equals(command);
        return HandleResult.success()
                .state(new AdminsManipulateFrame(isAdd))
                .build();
    }
}
