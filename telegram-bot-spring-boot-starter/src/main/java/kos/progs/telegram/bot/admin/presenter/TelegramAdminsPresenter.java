package kos.progs.telegram.bot.admin.presenter;

import kos.progs.telegram.bot.admin.button.AdminsCommand;
import kos.progs.telegram.bot.admin.service.AdminService;
import kos.progs.telegram.bot.common.model.Lang;
import kos.progs.telegram.bot.core.model.HandleResult;
import kos.progs.telegram.bot.i18n.service.MessagesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TelegramAdminsPresenter {
    private final AdminService adminService;
    private final AdminsCommand adminsCommand;
    private final MessagesService messagesService;
    private final TelegramClient telegramClient;

    public HandleResult present(Lang lang, long chatId, long adminId) throws TelegramApiException {
        if (!adminService.isUserAdmin(adminId)) {
            return HandleResult.unhandled();
        }

        AtomicInteger i = new AtomicInteger();
        String text = messagesService.get("admin.admins.title", lang)
                + "\n"
                + adminService.getAdmins(adminId)
                .stream()
                .map(vl -> messagesService.get("admin.admins.concrete", lang, i.incrementAndGet(), vl))
                .collect(Collectors.joining("\n"));

        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .parseMode(ParseMode.HTML)
                .replyMarkup(new InlineKeyboardMarkup(List.of(
                        new InlineKeyboardRow(
                                InlineKeyboardButton.builder()
                                        .text(messagesService.get("admin.admins.add", lang))
                                        .callbackData(adminsCommand.buildAddAdminCommand())
                                        .build()
                        ),
                        new InlineKeyboardRow(
                                InlineKeyboardButton.builder()
                                        .text(messagesService.get("admin.admins.remove", lang))
                                        .callbackData(adminsCommand.buildRemoveAdminCommand())
                                        .build()
                        )
                )))
                .build();

        telegramClient.execute(sendMessage);
        return HandleResult.success().build();
    }
}
