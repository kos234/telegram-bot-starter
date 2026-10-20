package kos.progs.telegram.bot.settings.spi;

import kos.progs.telegram.bot.common.model.Lang;
import kos.progs.telegram.bot.i18n.service.MessagesService;
import kos.progs.telegram.bot.settings.button.ChangeUserInfoCommand;
import kos.progs.telegram.bot.user.model.UserInfo;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class EmailSettingsRowContributor implements SettingsRowContributor {
    private final MessagesService messagesService;
    private final ChangeUserInfoCommand changeUserInfoCommand;

    @Override
    public int getOrder() {
        return 300;
    }

    @Override
    public String getExtraInfo(UserInfo userInfo) {
        Lang lang = userInfo.lang() != null ? userInfo.lang() : Lang.RU;
        String emailVal = Optional.ofNullable(userInfo.email()).orElse(messagesService.get("settings.empty", lang));
        return messagesService.get("settings.email", lang, emailVal);
    }

    @Override
    public List<InlineKeyboardRow> getRows(UserInfo userInfo) {
        Lang lang = userInfo.lang() != null ? userInfo.lang() : Lang.RU;
        return List.of(new InlineKeyboardRow(
                InlineKeyboardButton.builder()
                        .text(messagesService.get("settings.email", lang, messagesService.get("settings.change_button", lang)))
                        .callbackData(changeUserInfoCommand.buildEmailChangeCommand())
                        .build()
        ));
    }
}
