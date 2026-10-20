package kos.progs.telegram.bot.common.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import kos.progs.telegram.bot.user.model.UserInfo;
import kos.progs.telegram.bot.user.repository.jpa.CurrencyJpaConverter;
import kos.progs.telegram.bot.user.repository.jpa.LangJpaConverter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class LangAndCurrencySerializationTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final LangJpaConverter langConverter = new LangJpaConverter();
    private final CurrencyJpaConverter currencyConverter = new CurrencyJpaConverter();

    @BeforeEach
    @AfterEach
    void resetRegistry() {
        Lang.reset();
        Currency.reset();
    }

    @Test
    void testJacksonSerializationAndDeserialization() throws Exception {
        Lang de = Lang.register((byte) 2, "DE", Locale.GERMAN, "Deutsch");
        Currency eur = Currency.register((byte) 2, "EUR", "€");

        UserInfo original = new UserInfo(100L, de, eur, "test@example.com", "79991234567");

        String json = objectMapper.writeValueAsString(original);
        assertTrue(json.contains("\"lang\":\"DE\""), "JSON should contain 'DE'");
        assertTrue(json.contains("\"currency\":\"EUR\""), "JSON should contain 'EUR'");

        UserInfo deserialized = objectMapper.readValue(json, UserInfo.class);
        assertEquals(original.userId(), deserialized.userId());
        assertEquals(de, deserialized.lang());
        assertEquals(eur, deserialized.currency());
        assertEquals("test@example.com", deserialized.email());
        assertEquals("79991234567", deserialized.phone());
    }

    @Test
    void testJacksonWithNullLangAndCurrency() throws Exception {
        UserInfo original = new UserInfo(101L, null, null, null, null);
        String json = objectMapper.writeValueAsString(original);

        UserInfo deserialized = objectMapper.readValue(json, UserInfo.class);
        assertEquals(101L, deserialized.userId());
        assertNull(deserialized.lang());
        assertNull(deserialized.currency());
    }

    @Test
    void testJpaConverters() {
        Lang fr = Lang.register((byte) 3, "FR", Locale.FRENCH, "Français");
        Currency gbp = Currency.register((byte) 3, "GBP", "£");

        // LangJpaConverter
        assertEquals("FR", langConverter.convertToDatabaseColumn(fr));
        assertEquals(fr, langConverter.convertToEntityAttribute("FR"));
        assertEquals(fr, langConverter.convertToEntityAttribute("fr"));
        assertNull(langConverter.convertToDatabaseColumn(null));
        assertNull(langConverter.convertToEntityAttribute(null));
        assertNull(langConverter.convertToEntityAttribute(""));

        // CurrencyJpaConverter
        assertEquals("GBP", currencyConverter.convertToDatabaseColumn(gbp));
        assertEquals(gbp, currencyConverter.convertToEntityAttribute("GBP"));
        assertEquals(gbp, currencyConverter.convertToEntityAttribute("gbp"));
        assertNull(currencyConverter.convertToDatabaseColumn(null));
        assertNull(currencyConverter.convertToEntityAttribute(null));
        assertNull(currencyConverter.convertToEntityAttribute(""));
    }
}
