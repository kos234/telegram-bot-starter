package kos.progs.telegram.bot.common.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyTest {

    @BeforeEach
    @AfterEach
    void resetRegistry() {
        Currency.reset();
    }

    @Test
    void testPredefinedConstants() {
        assertNotNull(Currency.RUB);
        assertNotNull(Currency.USD);
        assertEquals((byte) 0, Currency.RUB.getId());
        assertEquals("RUB", Currency.RUB.getCode());
        assertEquals("RUB", Currency.RUB.name());
        assertEquals("₽", Currency.RUB.getTitle());

        assertEquals((byte) 1, Currency.USD.getId());
        assertEquals("USD", Currency.USD.getCode());
        assertEquals("$", Currency.USD.getTitle());
    }

    @Test
    void testValueOfByIdAndName() {
        assertEquals(Currency.RUB, Currency.valueOf((byte) 0));
        assertEquals(Currency.USD, Currency.valueOf((byte) 1));
        assertEquals(Currency.RUB, Currency.valueOf("RUB"));
        assertEquals(Currency.RUB, Currency.valueOf("rub"));
        assertEquals(Currency.USD, Currency.valueOf("usd"));

        assertThrows(IllegalArgumentException.class, () -> Currency.valueOf((byte) 99));
        assertThrows(IllegalArgumentException.class, () -> Currency.valueOf("EUR"));
        assertThrows(IllegalArgumentException.class, () -> Currency.valueOf((String) null));
    }

    @Test
    void testSaveValueOf() {
        assertEquals(Currency.RUB, Currency.saveValueOf("RUB"));
        assertEquals(Currency.USD, Currency.saveValueOf("USD"));
        assertEquals(Currency.RUB, Currency.saveValueOf("₽"));
        assertEquals(Currency.USD, Currency.saveValueOf("$"));
        // fallback
        assertEquals(Currency.RUB, Currency.saveValueOf("nonexistent"));
        assertEquals(Currency.RUB, Currency.saveValueOf(null));
        assertEquals(Currency.RUB, Currency.saveValueOf(""));
    }

    @Test
    void testRegisterCustomCurrency() {
        Currency eur = Currency.register((byte) 2, "EUR", "€");
        assertNotNull(eur);
        assertEquals((byte) 2, eur.getId());
        assertEquals("EUR", eur.getCode());
        assertEquals("€", eur.getTitle());

        assertEquals(eur, Currency.valueOf((byte) 2));
        assertEquals(eur, Currency.valueOf("EUR"));
        assertEquals(eur, Currency.valueOf("eur"));
        assertEquals(eur, Currency.saveValueOf("EUR"));
        assertEquals(eur, Currency.saveValueOf("€"));

        boolean found = false;
        for (Currency c : Currency.values()) {
            if ("EUR".equals(c.getCode())) {
                found = true;
                break;
            }
        }
        assertTrue(found, "Registered custom currency EUR should be present in values()");
    }

    @Test
    void testUnregister() {
        Currency kzt = Currency.register((byte) 3, "KZT", "₸");
        assertEquals(kzt, Currency.valueOf("KZT"));

        assertTrue(Currency.unregister("KZT"));
        assertThrows(IllegalArgumentException.class, () -> Currency.valueOf("KZT"));
        assertThrows(IllegalArgumentException.class, () -> Currency.valueOf((byte) 3));
    }

    @Test
    void testEqualsAndHashCode() {
        Currency c1 = new Currency((byte) 5, "GBP", "£");
        Currency c2 = new Currency((byte) 5, "gbp", "£");
        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }
}
