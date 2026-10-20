package kos.progs.telegram.bot.admin.state;

import kos.progs.telegram.bot.admin.model.AdminsManipulateFrame;
import kos.progs.telegram.bot.admin.presenter.TelegramAdminsPresenter;
import kos.progs.telegram.bot.admin.service.AdminService;
import kos.progs.telegram.bot.core.model.HandleResult;
import kos.progs.telegram.bot.i18n.service.MessagesService;
import kos.progs.telegram.bot.states.handler.StateHandler;
import kos.progs.telegram.bot.states.model.StateFrame;
import kos.progs.telegram.bot.user.model.UserInfo;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@RequiredArgsConstructor
public class AdminsManipulateState implements StateHandler {
    private final MessagesService messagesService;
    private final TelegramAdminsPresenter telegramAdminsPresenter;
    private final AdminService adminService;
    private final TelegramClient telegramClient;

    @Override
    public boolean isHandle(StateFrame frame) {
        return frame instanceof AdminsManipulateFrame;
    }

    @Override
    public HandleResult handle(Message message, UserInfo userInfo, StateFrame frame) throws TelegramApiException {
        AdminsManipulateFrame args = (AdminsManipulateFrame) frame;
        String input = message.getText();
        var lang = userInfo.lang();

        long userId;
        try {
            userId = Long.parseLong(input);
        } catch (NumberFormatException e) {
            SendMessage err = SendMessage.builder()
                    .chatId(message.getChatId())
                    .text(messagesService.get("global.error.value_not_number", lang))
                    .build();
            telegramClient.execute(err);
            return HandleResult.error("Value not a number");
        }

        if (userId < 0) {
            SendMessage err = SendMessage.builder()
                    .chatId(message.getChatId())
                    .text(messagesService.get("global.error.negative_value", lang))
                    .build();
            telegramClient.execute(err);
            return HandleResult.error("Negative value");
        }

        long adminId = message.getFrom().getId();
        boolean res;
        if (args.isAdd()) {
            res = adminService.setAdmin(adminId, userId);
        } else {
            res = adminService.removeAdmin(adminId, userId);
        }

        if (res) {
            return telegramAdminsPresenter.present(lang, message.getChatId(), adminId).withUpdateState(true);
        }

        SendMessage err = SendMessage.builder()
                .chatId(message.getChatId())
                .text(messagesService.get("admin.admins.error_manipulation", lang))
                .build();
        telegramClient.execute(err);
        return HandleResult.error("Manipulation failed");
    }
}
