package kos.progs.telegram.bot.settings.spi;

import kos.progs.telegram.bot.common.model.Currency;
import kos.progs.telegram.bot.common.model.Lang;
import kos.progs.telegram.bot.i18n.service.MessagesService;
import kos.progs.telegram.bot.settings.button.ChangeCurrencyCommand;
import kos.progs.telegram.bot.user.model.UserInfo;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class CurrencySettingsRowContributor implements SettingsRowContributor {
    private final MessagesService messagesService;
    private final ChangeCurrencyCommand changeCurrencyCommand;

    @Override
    public int getOrder() {
        return 200;
    }

    @Override
    public String getExtraInfo(UserInfo userInfo) {
        if (userInfo.currency() == null) {
            return "";
        }
        Lang lang = userInfo.lang() != null ? userInfo.lang() : Lang.RU;
        return messagesService.get("settings.currency_choose", lang, userInfo.currency().getTitle());
    }

    @Override
    public List<InlineKeyboardRow> getRows(UserInfo userInfo) {
        if (userInfo.currency() == null) {
            return List.of();
        }
        Lang currentLang = userInfo.lang() != null ? userInfo.lang() : Lang.RU;
        List<InlineKeyboardRow> rows = new ArrayList<>();

        // Header
        rows.add(new InlineKeyboardRow(
                InlineKeyboardButton.builder()
                        .text(messagesService.get("settings.currency_choose", currentLang, userInfo.currency().getTitle()))
                        .callbackData("ignore")
                        .build()
        ));

        // Currency buttons
        Currency[] currencies = Currency.values();
        List<InlineKeyboardButton> currentRowButtons = new ArrayList<>();
        for (Currency c : currencies) {
            currentRowButtons.add(
                    InlineKeyboardButton.builder()
                            .text(c.getTitle())
                            .callbackData(changeCurrencyCommand.buildCommand(c))
                            .build()
            );
            if (currentRowButtons.size() == 2) {
                rows.add(new InlineKeyboardRow(new ArrayList<>(currentRowButtons)));
                currentRowButtons.clear();
            }
        }
        if (!currentRowButtons.isEmpty()) {
            rows.add(new InlineKeyboardRow(currentRowButtons));
        }

        return rows;
    }
}
