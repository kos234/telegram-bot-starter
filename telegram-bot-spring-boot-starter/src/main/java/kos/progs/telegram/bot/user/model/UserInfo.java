package kos.progs.telegram.bot.user.model;

import kos.progs.telegram.bot.common.model.Currency;
import kos.progs.telegram.bot.common.model.Lang;
import lombok.Builder;
import lombok.With;

@Builder(toBuilder = true)
@With
public record UserInfo(long userId, Lang lang, Currency currency, String email, String phone) {
    public static UserInfo firstInfo(long userId, Lang lang, Currency currency) {
        return new UserInfo(userId, lang, currency, null, null);
    }
}
