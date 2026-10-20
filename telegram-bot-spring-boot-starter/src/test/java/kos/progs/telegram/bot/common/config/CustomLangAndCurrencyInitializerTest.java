package kos.progs.telegram.bot.common.config;

import kos.progs.telegram.bot.common.model.Currency;
import kos.progs.telegram.bot.common.model.Lang;
import kos.progs.telegram.bot.core.config.TelegramBotProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class CustomLangAndCurrencyInitializerTest {

    @BeforeEach
    @AfterEach
    void reset() {
        Lang.reset();
        Currency.reset();
    }

    @Test
    void testInitFromProperties() {
        TelegramBotProperties properties = new TelegramBotProperties();

        TelegramBotProperties.CustomLanguageProperties customLang = new TelegramBotProperties.CustomLanguageProperties();
        customLang.setId((byte) 5);
        customLang.setCode("IT");
        customLang.setTag("it-IT");
        customLang.setTitle("Italiano");
        properties.getLanguage().setCustom(List.of(customLang));

        TelegramBotProperties.CustomCurrencyProperties customCurrency = new TelegramBotProperties.CustomCurrencyProperties();
        customCurrency.setId((byte) 5);
        customCurrency.setCode("JPY");
        customCurrency.setTitle("¥");
        properties.getCurrency().setCustom(List.of(customCurrency));

        CustomLangAndCurrencyInitializer initializer =
                new CustomLangAndCurrencyInitializer(properties, null, null);
        initializer.init();

        assertEquals("IT", Lang.valueOf((byte) 5).getCode());
        assertEquals("Italiano", Lang.valueOf("IT").getTitle());

        assertEquals("JPY", Currency.valueOf((byte) 5).getCode());
        assertEquals("¥", Currency.valueOf("JPY").getTitle());
    }

    @Test
    void testInitFromBeans() {
        Lang es = new Lang((byte) 6, "ES", Locale.forLanguageTag("es"), "Español");
        Currency cny = new Currency((byte) 6, "CNY", "¥");

        CustomLangAndCurrencyInitializer initializer =
                new CustomLangAndCurrencyInitializer(null, List.of(es), List.of(cny));
        initializer.init();

        assertEquals(es, Lang.valueOf("ES"));
        assertEquals(cny, Currency.valueOf("CNY"));
    }
}
