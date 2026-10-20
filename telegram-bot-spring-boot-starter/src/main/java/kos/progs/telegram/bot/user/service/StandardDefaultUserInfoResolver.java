package kos.progs.telegram.bot.user.service;

import kos.progs.telegram.bot.common.model.Currency;
import kos.progs.telegram.bot.common.model.Lang;
import kos.progs.telegram.bot.core.config.TelegramBotProperties;
import kos.progs.telegram.bot.user.model.UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@RequiredArgsConstructor
public class StandardDefaultUserInfoResolver implements DefaultUserInfoResolver {
    private static final Set<String> RU_REGION_LANGS = Set.of(
            "ru", "by", "kz", "kg", "ua", "am", "az", "ge", "uz", "tj", "md", "lv", "lt", "ee", "il"
    );

    private final TelegramBotProperties properties;

    @Override
    public UserInfo createDefaultUserInfo(long userId, String systemLang) {
        Lang lang = resolveDefaultLang(systemLang);
        Currency currency = resolveDefaultCurrency(systemLang);
        return UserInfo.firstInfo(userId, lang, currency);
    }

    @Override
    public Lang resolveDefaultLang(String systemLang) {
        if (properties != null && !properties.getLanguage().isEnabled()) {
            return getDefaultConfiguredLang();
        }

        if (systemLang != null && !systemLang.isBlank()) {
            String lower = systemLang.toLowerCase();
            if (RU_REGION_LANGS.contains(lower)) {
                return Lang.RU;
            }
            try {
                return Lang.valueOf(lower);
            } catch (Exception ignored) {
            }
            return Lang.EN;
        }

        return getDefaultConfiguredLang();
    }

    @Override
    public Currency resolveDefaultCurrency(String systemLang) {
        if (properties != null && !properties.getCurrency().isEnabled()) {
            return null;
        }

        if (systemLang != null && !systemLang.isBlank()) {
            String lower = systemLang.toLowerCase();
            if (RU_REGION_LANGS.contains(lower)) {
                return Currency.RUB;
            }
            return Currency.USD;
        }

        return getDefaultConfiguredCurrency();
    }

    protected Lang getDefaultConfiguredLang() {
        if (properties != null && properties.getLanguage().getDefaultCode() != null) {
            return Lang.saveValueOf(properties.getLanguage().getDefaultCode());
        }
        return Lang.RU;
    }

    protected Currency getDefaultConfiguredCurrency() {
        if (properties != null && properties.getCurrency().getDefaultCode() != null) {
            return Currency.saveValueOf(properties.getCurrency().getDefaultCode());
        }
        return Currency.RUB;
    }
}
