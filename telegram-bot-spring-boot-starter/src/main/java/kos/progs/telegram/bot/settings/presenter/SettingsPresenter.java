package kos.progs.telegram.bot.settings.presenter;

import kos.progs.telegram.bot.command.handler.CancelHandler;
import kos.progs.telegram.bot.common.model.Lang;
import kos.progs.telegram.bot.core.model.HandleResult;
import kos.progs.telegram.bot.i18n.service.MessagesService;
import kos.progs.telegram.bot.settings.button.ChangeCurrencyCommand;
import kos.progs.telegram.bot.settings.button.ChangeLangCommand;
import kos.progs.telegram.bot.settings.button.ChangeUserInfoCommand;
import kos.progs.telegram.bot.settings.spi.SettingsRowContributor;
import kos.progs.telegram.bot.user.model.UserInfo;
import kos.progs.telegram.bot.user.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class SettingsPresenter {
    private final MessagesService messagesService;
    private final TelegramClient telegramClient;
    private final CancelHandler cancelHandler;
    private final List<SettingsRowContributor> contributors;

    @Autowired
    public SettingsPresenter(MessagesService messagesService,
                             TelegramClient telegramClient,
                             CancelHandler cancelHandler,
                             List<SettingsRowContributor> contributors) {
        this.messagesService = messagesService;
        this.telegramClient = telegramClient;
        this.cancelHandler = cancelHandler;
        this.contributors = contributors != null ? contributors : List.of();
    }

    public SettingsPresenter(MessagesService messagesService,
                             TelegramClient telegramClient,
                             CancelHandler cancelHandler,
                             UserInfoService userInfoService,
                             ChangeUserInfoCommand changeUserInfoCommand,
                             List<SettingsRowContributor> contributors) {
        this(messagesService, telegramClient, cancelHandler, contributors);
    }

    public SettingsPresenter(MessagesService messagesService,
                             TelegramClient telegramClient,
                             CancelHandler cancelHandler,
                             ChangeLangCommand changeLangCommand,
                             ChangeCurrencyCommand changeCurrencyCommand,
                             UserInfoService userInfoService,
                             ChangeUserInfoCommand changeUserInfoCommand,
                             List<SettingsRowContributor> contributors) {
        this(messagesService, telegramClient, cancelHandler, contributors);
    }

    public HandleResult presentSettings(UserInfo presentedUserInfo, long chatId) throws TelegramApiException {
        return presentSettings(presentedUserInfo, chatId, -1);
    }

    public HandleResult presentSettings(UserInfo presentedUserInfo, long chatId, int editMessageId) throws TelegramApiException {
        if (editMessageId == -1) {
            sendMessage(presentedUserInfo, chatId);
        } else {
            editMessage(presentedUserInfo, chatId, editMessageId);
        }
        return HandleResult.success().state(null).build();
    }

    private void sendMessage(UserInfo userInfo, long chatId) throws TelegramApiException {
        SendMessage sendMessage = SendMessage.builder()
                .text(getMessage(userInfo))
                .chatId(chatId)
                .replyMarkup(getKeyboard(userInfo))
                .build();
        telegramClient.execute(sendMessage);
    }

    private void editMessage(UserInfo userInfo, long chatId, int messageId) throws TelegramApiException {
        EditMessageText editMessageText = EditMessageText.builder()
                .text(getMessage(userInfo))
                .chatId(chatId)
                .messageId(messageId)
                .replyMarkup(getKeyboard(userInfo))
                .build();
        telegramClient.execute(editMessageText);
    }

    private String getMessage(UserInfo userInfo) {
        Lang lang = userInfo.lang() != null ? userInfo.lang() : Lang.RU;
        StringBuilder sb = new StringBuilder();

        String intro = messagesService.get("settings.message_intro", lang);
        if (intro == null || intro.equals("settings.message_intro")) {
            intro = messagesService.get("settings.title", lang);
        }
        sb.append(intro).append("\n");

        contributors.stream()
                .sorted(Comparator.comparingInt(SettingsRowContributor::getOrder))
                .forEach(c -> {
                    String info = c.getExtraInfo(userInfo);
                    if (info != null && !info.isBlank()) {
                        sb.append("\n").append(info);
                    }
                });

        return sb.toString();
    }

    public InlineKeyboardMarkup getKeyboard(UserInfo userInfo) {
        Lang lang = userInfo.lang() != null ? userInfo.lang() : Lang.RU;

        List<InlineKeyboardRow> rows = new ArrayList<>();

        contributors.stream()
                .sorted(Comparator.comparingInt(SettingsRowContributor::getOrder))
                .forEach(c -> {
                    List<InlineKeyboardRow> contributorRows = c.getRows(userInfo);
                    if (contributorRows != null) {
                        rows.addAll(contributorRows);
                    }
                });

        // Back button
        rows.add(new InlineKeyboardRow(
                InlineKeyboardButton.builder().text(" ").callbackData("ignore").build()
        ));
        rows.add(new InlineKeyboardRow(cancelHandler.getInlineKeyboardButton(lang)));

        return new InlineKeyboardMarkup(rows);
    }
}
