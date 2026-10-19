package kos.progs.telegram.bot.admin.command;

import kos.progs.telegram.bot.admin.button.AdminsCommand;
import kos.progs.telegram.bot.admin.service.AdminService;
import kos.progs.telegram.bot.command.handler.CommandHandler;
import kos.progs.telegram.bot.core.model.HandleResult;
import kos.progs.telegram.bot.i18n.service.MessagesService;
import kos.progs.telegram.bot.spi.AdminMenuItemProvider;
import kos.progs.telegram.bot.user.model.UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminPanelCommand implements CommandHandler {
    private final MessagesService messagesService;
    private final AdminService adminService;
    private final TelegramClient telegramClient;
    private final AdminsCommand adminsCommand;
    private final List<AdminMenuItemProvider> menuItemProviders;

    @Override
    public boolean isHandle(String command, String lastHistory) {
        return command.equals("/admin") || messagesService.getAll("admin.title").contains(command);
    }

    @Override
    public HandleResult handle(Message message, UserInfo userInfo, String command) throws TelegramApiException {
        if (!adminService.isUserAdmin(message.getFrom().getId())) {
            return HandleResult.unhandled();
        }

        var lang = userInfo.lang();
        List<InlineKeyboardRow> rows = new ArrayList<>();

        // Staff management row is always present in starter
        rows.add(new InlineKeyboardRow(
                InlineKeyboardButton.builder()
                        .text(messagesService.get("admin.staff", lang))
                        .callbackData(adminsCommand.buildViewListCommand())
                        .build()
        ));

        // Additional items contributed by providers
        if (menuItemProviders != null) {
            menuItemProviders.stream()
                    .sorted(Comparator.comparingInt(AdminMenuItemProvider::getOrder))
                    .forEach(provider -> {
                        InlineKeyboardRow row = provider.getButtonRow(message.getFrom().getId(), lang);
                        if (row != null) {
                            rows.add(row);
                        }
                    });
        }

        SendMessage sendMessage = SendMessage.builder()
                .chatId(message.getChatId())
                .text(messagesService.get("admin.title", lang))
                .replyMarkup(new InlineKeyboardMarkup(rows))
                .build();

        telegramClient.execute(sendMessage);
        return HandleResult.success().state(null).build();
    }
}
