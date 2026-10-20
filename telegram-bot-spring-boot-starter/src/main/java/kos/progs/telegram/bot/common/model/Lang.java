package kos.progs.telegram.bot.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class Lang implements Serializable {
    private static final Map<Byte, Lang> BY_ID = new ConcurrentHashMap<>();
    private static final Map<String, Lang> BY_CODE = new ConcurrentHashMap<>();

    public static final Lang RU = register(new Lang((byte) 0, "RU", Locale.forLanguageTag("ru"), "Русский"));
    public static final Lang EN = register(new Lang((byte) 1, "EN", Locale.ENGLISH, "English"));

    private final byte id;
    private final String code;
    private final Locale locale;
    private final String title;

    public Lang(byte id, Locale locale) {
        this(id, locale != null ? locale.getLanguage() : "unknown", locale, null);
    }

    public Lang(byte id, String code, Locale locale) {
        this(id, code, locale, null);
    }

    public Lang(byte id, String code, Locale locale, String title) {
        this.id = id;
        this.code = Objects.requireNonNull(code, "code must not be null").toUpperCase();
        this.locale = locale != null ? locale : Locale.forLanguageTag(code.toLowerCase());
        this.title = title != null && !title.isBlank() ? title : this.code;
    }

    public static synchronized Lang register(Lang lang) {
        Objects.requireNonNull(lang, "lang must not be null");
        BY_ID.put(lang.getId(), lang);
        BY_CODE.put(lang.getCode().toUpperCase(), lang);
        return lang;
    }

    public static Lang register(byte id, String code, Locale locale) {
        return register(new Lang(id, code, locale));
    }

    public static Lang register(byte id, String code, Locale locale, String title) {
        return register(new Lang(id, code, locale, title));
    }

    public static synchronized boolean unregister(Lang lang) {
        if (lang == null) return false;
        BY_ID.remove(lang.getId());
        BY_CODE.remove(lang.getCode().toUpperCase());
        return true;
    }

    public static synchronized boolean unregister(String code) {
        if (code == null) return false;
        Lang lang = BY_CODE.remove(code.toUpperCase());
        if (lang != null) {
            BY_ID.remove(lang.getId());
            return true;
        }
        return false;
    }

    public static synchronized void reset() {
        BY_ID.clear();
        BY_CODE.clear();
        register(RU);
        register(EN);
    }

    @JsonValue
    public String name() {
        return code;
    }

    public String getCode() {
        return code;
    }

    public static Lang valueOf(byte id) {
        Lang lang = BY_ID.get(id);
        if (lang != null) {
            return lang;
        }
        throw new IllegalArgumentException("Not found lang with id = " + id);
    }

    public static Lang valueOf(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Lang name must not be null");
        }
        Lang lang = BY_CODE.get(name.toUpperCase());
        if (lang != null) {
            return lang;
        }
        throw new IllegalArgumentException("Not found lang with name = " + name);
    }

    @JsonCreator
    public static Lang saveValueOf(String name) {
        if (name == null || name.isBlank()) {
            return RU;
        }
        Lang lang = BY_CODE.get(name.toUpperCase());
        if (lang != null) {
            return lang;
        }
        for (Lang l : BY_CODE.values()) {
            if (l.getCode().equalsIgnoreCase(name) || l.getTitle().equalsIgnoreCase(name)) {
                return l;
            }
        }
        return RU;
    }

    public static Lang[] values() {
        return BY_CODE.values().toArray(new Lang[0]);
    }

    public static Collection<Lang> all() {
        return Collections.unmodifiableCollection(BY_CODE.values());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Lang other)) return false;
        return id == other.id && code.equalsIgnoreCase(other.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, code.toUpperCase());
    }

    @Override
    public String toString() {
        return code;
    }
}
