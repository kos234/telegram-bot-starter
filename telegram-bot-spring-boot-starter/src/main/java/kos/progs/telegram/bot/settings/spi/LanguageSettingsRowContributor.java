package kos.progs.telegram.bot.settings.spi;

import kos.progs.telegram.bot.common.model.Lang;
import kos.progs.telegram.bot.i18n.service.MessagesService;
import kos.progs.telegram.bot.settings.button.ChangeLangCommand;
import kos.progs.telegram.bot.user.model.UserInfo;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class LanguageSettingsRowContributor implements SettingsRowContributor {
    private final MessagesService messagesService;
    private final ChangeLangCommand changeLangCommand;

    @Override
    public int getOrder() {
        return 100;
    }

    @Override
    public String getExtraInfo(UserInfo userInfo) {
        Lang lang = userInfo.lang() != null ? userInfo.lang() : Lang.RU;
        return messagesService.get("settings.lang_choose", lang, getLangTitle(lang));
    }

    @Override
    public List<InlineKeyboardRow> getRows(UserInfo userInfo) {
        Lang currentLang = userInfo.lang() != null ? userInfo.lang() : Lang.RU;
        List<InlineKeyboardRow> rows = new ArrayList<>();

        // Header
        rows.add(new InlineKeyboardRow(
                InlineKeyboardButton.builder()
                        .text(messagesService.get("settings.lang_choose", currentLang, getLangTitle(currentLang)))
                        .callbackData("ignore")
                        .build()
        ));

        // Language buttons
        Lang[] langs = Lang.values();
        List<InlineKeyboardButton> currentRowButtons = new ArrayList<>();
        for (Lang l : langs) {
            currentRowButtons.add(
                    InlineKeyboardButton.builder()
                            .text(getLangTitle(l))
                            .callbackData(changeLangCommand.buildCommand(l))
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

    public String getLangTitle(Lang lang) {
        if (lang == null) return "";
        if (lang.equals(Lang.RU)) {
            return messagesService.get("settings.lang.title", Lang.RU);
        }
        if (lang.equals(Lang.EN)) {
            return messagesService.get("settings.lang.title", Lang.EN);
        }
        String customKey = "settings.lang." + lang.getCode().toLowerCase() + ".title";
        try {
            String val = messagesService.get(customKey, lang);
            if (val != null && !val.equals(customKey)) {
                return val;
            }
        } catch (Exception ignored) {
        }
        return lang.getTitle();
    }
}
