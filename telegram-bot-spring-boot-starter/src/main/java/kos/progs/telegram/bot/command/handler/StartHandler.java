package kos.progs.telegram.bot.command.handler;

import kos.progs.telegram.bot.admin.service.AdminService;
import kos.progs.telegram.bot.common.model.Lang;
import kos.progs.telegram.bot.core.model.HandleResult;
import kos.progs.telegram.bot.i18n.service.MessagesService;
import kos.progs.telegram.bot.main_menu.MainMenuContributor;
import kos.progs.telegram.bot.states.service.NavigationService;
import kos.progs.telegram.bot.user.model.UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class StartHandler implements CommandHandler {
    private final TelegramClient telegramClient;
    private final NavigationService navigationService;
    private final MessagesService messagesService;
    private final AdminService adminService;
    private final List<MainMenuContributor> contributors;

    @Override
    public boolean isHandle(String command, String history) {
        return command.equals("/start") || messagesService.getAll("start.title").contains(command);
    }

    public ReplyKeyboardMarkup getStartKeyBoard(long userId, Lang lang) {
        List<KeyboardRow> rows = new ArrayList<>();
        if (contributors != null) {
            contributors.stream()
                    .sorted(Comparator.comparingInt(MainMenuContributor::getOrder))
                    .forEach(c -> {
                        List<KeyboardRow> cRows = c.getRows(userId, lang);
                        if (cRows != null) {
                            rows.addAll(cRows);
                        }
                    });
        }

        if (adminService.isUserAdmin(userId)) {
            rows.add(new KeyboardRow(messagesService.get("admin.title", lang)));
        }

        var replyKeyboardMarkup = new ReplyKeyboardMarkup(rows);
        replyKeyboardMarkup.setResizeKeyboard(true);
        return replyKeyboardMarkup;
    }

    @Override
    public HandleResult handle(Message message, UserInfo userInfo, String command) throws TelegramApiException {
        SendMessage sendMessage = SendMessage.builder()
                .text(messagesService.get("start.message", userInfo.lang()))
                .chatId(message.getChatId())
                .replyMarkup(getStartKeyBoard(message.getFrom().getId(), userInfo.lang()))
                .build();

        telegramClient.execute(sendMessage);
        navigationService.resetHistory(message.getChatId());
        return HandleResult.success().state(null).build();
    }
}
