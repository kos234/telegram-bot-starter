package kos.progs.telegram.bot.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class Currency implements Serializable {
    private static final Map<Byte, Currency> BY_ID = new ConcurrentHashMap<>();
    private static final Map<String, Currency> BY_CODE = new ConcurrentHashMap<>();

    public static final Currency RUB = register(new Currency((byte) 0, "RUB", "₽"));
    public static final Currency USD = register(new Currency((byte) 1, "USD", "$"));

    private final byte id;
    private final String code;
    private final String title;

    public Currency(byte id, String title) {
        this(id, title, title);
    }

    public Currency(byte id, String code, String title) {
        this.id = id;
        this.code = Objects.requireNonNull(code, "code must not be null").toUpperCase();
        this.title = title != null && !title.isBlank() ? title : this.code;
    }

    public static synchronized Currency register(Currency currency) {
        Objects.requireNonNull(currency, "currency must not be null");
        BY_ID.put(currency.getId(), currency);
        BY_CODE.put(currency.getCode().toUpperCase(), currency);
        return currency;
    }

    public static Currency register(byte id, String code, String title) {
        return register(new Currency(id, code, title));
    }

    public static synchronized boolean unregister(Currency currency) {
        if (currency == null) return false;
        BY_ID.remove(currency.getId());
        BY_CODE.remove(currency.getCode().toUpperCase());
        return true;
    }

    public static synchronized boolean unregister(String code) {
        if (code == null) return false;
        Currency c = BY_CODE.remove(code.toUpperCase());
        if (c != null) {
            BY_ID.remove(c.getId());
            return true;
        }
        return false;
    }

    public static synchronized void reset() {
        BY_ID.clear();
        BY_CODE.clear();
        register(RUB);
        register(USD);
    }

    @JsonValue
    public String name() {
        return code;
    }

    public String getCode() {
        return code;
    }

    public static Currency valueOf(byte id) {
        Currency c = BY_ID.get(id);
        if (c != null) {
            return c;
        }
        throw new IllegalArgumentException("Not found currency with id = " + id);
    }

    public static Currency valueOf(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Currency name must not be null");
        }
        Currency c = BY_CODE.get(name.toUpperCase());
        if (c != null) {
            return c;
        }
        throw new IllegalArgumentException("Not found currency with name = " + name);
    }

    @JsonCreator
    public static Currency saveValueOf(String name) {
        if (name == null || name.isBlank()) {
            return RUB;
        }
        Currency c = BY_CODE.get(name.toUpperCase());
        if (c != null) {
            return c;
        }
        for (Currency cur : BY_CODE.values()) {
            if (cur.getCode().equalsIgnoreCase(name) || cur.getTitle().equalsIgnoreCase(name)) {
                return cur;
            }
        }
        return RUB;
    }

    public static Currency[] values() {
        return BY_CODE.values().toArray(new Currency[0]);
    }

    public static Collection<Currency> all() {
        return Collections.unmodifiableCollection(BY_CODE.values());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Currency other)) return false;
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
