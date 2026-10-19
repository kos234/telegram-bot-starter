package kos.progs.telegram.bot.common.model;

import lombok.Getter;

@Getter
public enum Currency {
    RUB((byte) 0, "₽"),
    USD((byte) 1, "$");

    private final byte id;
    private final String title;

    Currency(byte id, String title) {
        this.id = id;
        this.title = title;
    }

    public static Currency valueOf(byte id) {
        for (Currency c : values()) {
            if (c.getId() == id)
                return c;
        }
        throw new IllegalArgumentException("Not found currency with id = " + id);
    }

    public static Currency saveValueOf(String name) {
        for (Currency c : values()) {
            if (c.name().equalsIgnoreCase(name) || c.getTitle().equals(name))
                return c;
        }
        return RUB;
    }
}
