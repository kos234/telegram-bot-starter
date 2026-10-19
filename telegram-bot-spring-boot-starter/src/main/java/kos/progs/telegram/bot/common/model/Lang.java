package kos.progs.telegram.bot.common.model;

import lombok.Getter;

import java.util.Locale;

@Getter
public enum Lang {
    RU((byte) 0, Locale.forLanguageTag("ru")),
    EN((byte) 1, Locale.ENGLISH);

    private final byte id;
    private final Locale locale;

    Lang(byte id, Locale locale) {
        this.id = id;
        this.locale = locale;
    }

    public static Lang valueOf(byte id) {
        for (Lang c : values()) {
            if (c.getId() == id)
                return c;
        }
        throw new IllegalArgumentException("Not found lang with id = " + id);
    }
}
