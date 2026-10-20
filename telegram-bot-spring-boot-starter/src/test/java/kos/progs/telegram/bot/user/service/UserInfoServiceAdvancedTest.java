package kos.progs.telegram.bot.user.service;

import kos.progs.telegram.bot.common.model.Currency;
import kos.progs.telegram.bot.common.model.Lang;
import kos.progs.telegram.bot.core.config.TelegramBotProperties;
import kos.progs.telegram.bot.user.model.UserInfo;
import kos.progs.telegram.bot.user.repository.memory.MemoryUserInfoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class UserInfoServiceAdvancedTest {

    @BeforeEach
    @AfterEach
    void reset() {
        Lang.reset();
        Currency.reset();
    }

    @Test
    void testLanguageDisabledWithCustomDefaultLang() {
        TelegramBotProperties properties = new TelegramBotProperties();
        properties.getLanguage().setEnabled(false);
        properties.getLanguage().setDefaultCode("EN");

        DefaultUserInfoResolver resolver = new StandardDefaultUserInfoResolver(properties);
        UserInfoService service = new UserInfoService(new MemoryUserInfoRepository(), resolver);

        // Even with systemLang = "ru", it should return default EN because language selection is disabled
        UserInfo info = service.getUserInfo(1L, "ru");
        assertNotNull(info);
        assertEquals(Lang.EN, info.lang());
        assertEquals(Currency.RUB, info.currency());
    }

    @Test
    void testCurrencyDisabled() {
        TelegramBotProperties properties = new TelegramBotProperties();
        properties.getCurrency().setEnabled(false);

        DefaultUserInfoResolver resolver = new StandardDefaultUserInfoResolver(properties);
        UserInfoService service = new UserInfoService(new MemoryUserInfoRepository(), resolver);

        UserInfo info = service.getUserInfo(2L, "ru");
        assertNotNull(info);
        assertEquals(Lang.RU, info.lang());
        assertNull(info.currency(), "When currency is disabled, currency should be null");
    }

    @Test
    void testBothLanguageAndCurrencyDisabled() {
        TelegramBotProperties properties = new TelegramBotProperties();
        properties.getLanguage().setEnabled(false);
        properties.getLanguage().setDefaultCode("RU");
        properties.getCurrency().setEnabled(false);

        DefaultUserInfoResolver resolver = new StandardDefaultUserInfoResolver(properties);
        UserInfoService service = new UserInfoService(new MemoryUserInfoRepository(), resolver);

        UserInfo info = service.getUserInfo(3L, "en");
        assertNotNull(info);
        assertEquals(Lang.RU, info.lang());
        assertNull(info.currency());
    }

    @Test
    void testCustomDefaultUserInfoResolver() {
        Lang de = Lang.register((byte) 2, "DE", Locale.GERMAN, "Deutsch");
        Currency eur = Currency.register((byte) 2, "EUR", "€");

        // Custom resolver implementation provided by external project
        DefaultUserInfoResolver customResolver = new DefaultUserInfoResolver() {
            @Override
            public UserInfo createDefaultUserInfo(long userId, String systemLang) {
                if ("de".equalsIgnoreCase(systemLang)) {
                    return UserInfo.firstInfo(userId, de, eur);
                }
                return UserInfo.firstInfo(userId, Lang.EN, Currency.USD);
            }
        };

        UserInfoService service = new UserInfoService(new MemoryUserInfoRepository(), customResolver);

        UserInfo deUser = service.getUserInfo(4L, "de");
        assertEquals(de, deUser.lang());
        assertEquals(eur, deUser.currency());

        UserInfo otherUser = service.getUserInfo(5L, "fr");
        assertEquals(Lang.EN, otherUser.lang());
        assertEquals(Currency.USD, otherUser.currency());
    }

    @Test
    void testSubclassingUserInfoServiceToOverrideDefaultResolution() {
        Lang es = Lang.register((byte) 4, "ES", Locale.forLanguageTag("es"), "Español");

        // Subclass extending UserInfoService
        class CustomUserInfoService extends UserInfoService {
            public CustomUserInfoService(MemoryUserInfoRepository repo) {
                super(repo);
            }

            @Override
            protected UserInfo createDefaultUserInfo(long userId, String systemLang) {
                if ("es".equalsIgnoreCase(systemLang)) {
                    return UserInfo.firstInfo(userId, es, Currency.USD);
                }
                return super.createDefaultUserInfo(userId, systemLang);
            }
        }

        CustomUserInfoService customService = new CustomUserInfoService(new MemoryUserInfoRepository());

        UserInfo esUser = customService.getUserInfo(6L, "es");
        assertEquals(es, esUser.lang());
        assertEquals(Currency.USD, esUser.currency());

        UserInfo ruUser = customService.getUserInfo(7L, "ru");
        assertEquals(Lang.RU, ruUser.lang());
        assertEquals(Currency.RUB, ruUser.currency());
    }
}
