package kos.progs.telegram.bot.common.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class LangTest {

    @BeforeEach
    @AfterEach
    void resetRegistry() {
        Lang.reset();
    }

    @Test
    void testPredefinedConstants() {
        assertNotNull(Lang.RU);
        assertNotNull(Lang.EN);
        assertEquals((byte) 0, Lang.RU.getId());
        assertEquals("RU", Lang.RU.getCode());
        assertEquals("RU", Lang.RU.name());
        assertEquals(Locale.forLanguageTag("ru"), Lang.RU.getLocale());

        assertEquals((byte) 1, Lang.EN.getId());
        assertEquals("EN", Lang.EN.getCode());
        assertEquals(Locale.ENGLISH, Lang.EN.getLocale());
    }

    @Test
    void testValueOfByIdAndName() {
        assertEquals(Lang.RU, Lang.valueOf((byte) 0));
        assertEquals(Lang.EN, Lang.valueOf((byte) 1));
        assertEquals(Lang.RU, Lang.valueOf("RU"));
        assertEquals(Lang.RU, Lang.valueOf("ru"));
        assertEquals(Lang.EN, Lang.valueOf("en"));

        assertThrows(IllegalArgumentException.class, () -> Lang.valueOf((byte) 99));
        assertThrows(IllegalArgumentException.class, () -> Lang.valueOf("UNKNOWN"));
        assertThrows(IllegalArgumentException.class, () -> Lang.valueOf((String) null));
    }

    @Test
    void testSaveValueOf() {
        assertEquals(Lang.RU, Lang.saveValueOf("RU"));
        assertEquals(Lang.EN, Lang.saveValueOf("EN"));
        assertEquals(Lang.RU, Lang.saveValueOf("Русский"));
        assertEquals(Lang.EN, Lang.saveValueOf("English"));
        // fallback to default
        assertEquals(Lang.RU, Lang.saveValueOf("nonexistent"));
        assertEquals(Lang.RU, Lang.saveValueOf(null));
        assertEquals(Lang.RU, Lang.saveValueOf(""));
    }

    @Test
    void testRegisterCustomLang() {
        Lang de = Lang.register((byte) 2, "DE", Locale.GERMAN, "Deutsch");
        assertNotNull(de);
        assertEquals((byte) 2, de.getId());
        assertEquals("DE", de.getCode());
        assertEquals(Locale.GERMAN, de.getLocale());
        assertEquals("Deutsch", de.getTitle());

        assertEquals(de, Lang.valueOf((byte) 2));
        assertEquals(de, Lang.valueOf("DE"));
        assertEquals(de, Lang.valueOf("de"));
        assertEquals(de, Lang.saveValueOf("Deutsch"));

        boolean found = false;
        for (Lang l : Lang.values()) {
            if ("DE".equals(l.getCode())) {
                found = true;
                break;
            }
        }
        assertTrue(found, "Registered custom language DE should be present in values()");
    }

    @Test
    void testUnregister() {
        Lang fr = Lang.register((byte) 3, "FR", Locale.FRENCH, "Français");
        assertEquals(fr, Lang.valueOf("FR"));

        assertTrue(Lang.unregister("FR"));
        assertThrows(IllegalArgumentException.class, () -> Lang.valueOf("FR"));
        assertThrows(IllegalArgumentException.class, () -> Lang.valueOf((byte) 3));
    }

    @Test
    void testEqualsAndHashCode() {
        Lang lang1 = new Lang((byte) 5, "IT", Locale.ITALIAN);
        Lang lang2 = new Lang((byte) 5, "it", Locale.ITALIAN);
        assertEquals(lang1, lang2);
        assertEquals(lang1.hashCode(), lang2.hashCode());
    }
}
