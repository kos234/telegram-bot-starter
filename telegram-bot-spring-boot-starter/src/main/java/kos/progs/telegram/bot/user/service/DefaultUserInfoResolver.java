package kos.progs.telegram.bot.user.service;

import kos.progs.telegram.bot.common.model.Currency;
import kos.progs.telegram.bot.common.model.Lang;
import kos.progs.telegram.bot.user.model.UserInfo;

public interface DefaultUserInfoResolver {
    UserInfo createDefaultUserInfo(long userId, String systemLang);

    default Lang resolveDefaultLang(String systemLang) {
        return Lang.RU;
    }

    default Currency resolveDefaultCurrency(String systemLang) {
        return Currency.RUB;
    }
}
