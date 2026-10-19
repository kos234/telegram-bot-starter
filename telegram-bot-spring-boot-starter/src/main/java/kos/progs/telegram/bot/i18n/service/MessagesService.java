package kos.progs.telegram.bot.i18n.service;

import kos.progs.telegram.bot.common.model.Lang;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessagesService {
    private final MessageSource messageSource;

    public String get(String key, Lang lang, Object... params) {
        return messageSource.getMessage(key, params, lang.getLocale());
    }

    public List<String> getAll(String key, Object... params) {
        return getAll(key, List.of(Lang.values()), params);
    }

    public List<String> getAll(String key, List<Lang> langs, Object... params) {
        List<String> list = new ArrayList<>(langs.size());
        for (Lang l : langs) {
            list.add(messageSource.getMessage(key, params, l.getLocale()));
        }
        return list;
    }
}
