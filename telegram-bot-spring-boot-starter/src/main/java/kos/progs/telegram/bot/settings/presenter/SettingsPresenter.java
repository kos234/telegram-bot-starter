package kos.progs.telegram.bot.settings.presenter;

import kos.progs.telegram.bot.command.handler.CancelHandler;
import kos.progs.telegram.bot.common.model.Currency;
import kos.progs.telegram.bot.common.model.Lang;
import kos.progs.telegram.bot.core.model.HandleResult;
import kos.progs.telegram.bot.i18n.service.MessagesService;
import kos.progs.telegram.bot.settings.button.ChangeCurrencyCommand;
import kos.progs.telegram.bot.settings.button.ChangeLangCommand;
import kos.progs.telegram.bot.settings.button.ChangeUserInfoCommand;
import kos.progs.telegram.bot.settings.spi.SettingsRowContributor;
import kos.progs.telegram.bot.user.model.UserInfo;
import kos.progs.telegram.bot.user.service.UserInfoService;
import lombok.RequiredArgsConstructor;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SettingsPresenter {
    private final MessagesService messagesService;
    private final TelegramClient telegramClient;
    private final CancelHandler cancelHandler;
    private final ChangeLangCommand changeLangCommand;
    private final ChangeCurrencyCommand changeCurrencyCommand;
    private final UserInfoService userInfoService;
    private final ChangeUserInfoCommand changeUserInfoCommand;
    private final List<SettingsRowContributor> contributors;

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
        StringBuilder extra = new StringBuilder();
        if (contributors != null) {
            contributors.stream()
                    .sorted(Comparator.comparingInt(SettingsRowContributor::getOrder))
                    .forEach(c -> {
                        String info = c.getExtraInfo(userInfo);
                        if (info != null && !info.isBlank()) {
                            extra.append("\n").append(info);
                        }
                    });
        }

        String baseText = messagesService.get("settings.message_text", userInfo.lang(),
                messagesService.get("settings.lang.title", userInfo.lang()),
                userInfo.currency().getTitle(),
                Optional.ofNullable(userInfo.email()).orElse(messagesService.get("settings.empty", userInfo.lang())),
                Optional.ofNullable(userInfo.phone()).map(userInfoService::formatPhoneNumber).orElse(messagesService.get("settings.empty", userInfo.lang()))
        );

        return baseText + extra.toString();
    }

    public InlineKeyboardMarkup getKeyboard(UserInfo userInfo) {
        Lang lang = userInfo.lang();
        Currency currency = userInfo.currency();

        List<InlineKeyboardRow> rows = new ArrayList<>();

        // Language selection
        rows.add(new InlineKeyboardRow(
                InlineKeyboardButton.builder()
                        .text(messagesService.get("settings.lang_choose", lang, messagesService.get("settings.lang.title", lang)))
                        .callbackData("ignore")
                        .build()
        ));
        rows.add(new InlineKeyboardRow(
                InlineKeyboardButton.builder()
                        .text(messagesService.get("settings.lang.title", Lang.RU))
                        .callbackData(changeLangCommand.buildCommand(Lang.RU))
                        .build(),
                InlineKeyboardButton.builder()
                        .text(messagesService.get("settings.lang.title", Lang.EN))
                        .callbackData(changeLangCommand.buildCommand(Lang.EN))
                        .build()
        ));

        // Currency selection
        rows.add(new InlineKeyboardRow(
                InlineKeyboardButton.builder()
                        .text(messagesService.get("settings.currency_choose", lang, currency.getTitle()))
                        .callbackData("ignore")
                        .build()
        ));
        rows.add(new InlineKeyboardRow(
                InlineKeyboardButton.builder()
                        .text(Currency.RUB.getTitle())
                        .callbackData(changeCurrencyCommand.buildCommand(Currency.RUB))
                        .build(),
                InlineKeyboardButton.builder()
                        .text(Currency.USD.getTitle())
                        .callbackData(changeCurrencyCommand.buildCommand(Currency.USD))
                        .build()
        ));

        // Email & Phone
        rows.add(new InlineKeyboardRow(
                InlineKeyboardButton.builder()
                        .text(messagesService.get("settings.email", lang, messagesService.get("settings.change_button", lang)))
                        .callbackData(changeUserInfoCommand.buildEmailChangeCommand())
                        .build()
        ));
        rows.add(new InlineKeyboardRow(
                InlineKeyboardButton.builder()
                        .text(messagesService.get("settings.phone", lang, messagesService.get("settings.change_button", lang)))
                        .callbackData(changeUserInfoCommand.buildPhoneChangeCommand())
                        .build()
        ));

        // SPI extra rows
        if (contributors != null) {
            contributors.stream()
                    .sorted(Comparator.comparingInt(SettingsRowContributor::getOrder))
                    .forEach(c -> {
                        List<InlineKeyboardRow> extraRows = c.getRows(userInfo);
                        if (extraRows != null) {
                            rows.addAll(extraRows);
                        }
                    });
        }

        // Back button
        rows.add(new InlineKeyboardRow(
                InlineKeyboardButton.builder().text(" ").callbackData("ignore").build()
        ));
        rows.add(new InlineKeyboardRow(cancelHandler.getInlineKeyboardButton(lang)));

        return new InlineKeyboardMarkup(rows);
    }
}
