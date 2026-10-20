package kos.progs.telegram.bot.i18n.service;

import kos.progs.telegram.bot.common.model.Lang;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
public class MessagesService {
    private final MessageSource messageSource;

    public String get(String key, Lang lang, Object... params) {
        Locale locale = lang != null ? lang.getLocale() : Locale.getDefault();
        return messageSource.getMessage(key, params, key, locale);
    }

    public List<String> getAll(String key, Object... params) {
        return getAll(key, List.of(Lang.values()), params);
    }

    public List<String> getAll(String key, List<Lang> langs, Object... params) {
        List<String> list = new ArrayList<>(langs.size());
        for (Lang l : langs) {
            Locale locale = l != null ? l.getLocale() : Locale.getDefault();
            list.add(messageSource.getMessage(key, params, key, locale));
        }
        return list;
    }
}
