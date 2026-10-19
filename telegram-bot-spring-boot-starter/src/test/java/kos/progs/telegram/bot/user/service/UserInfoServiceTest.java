package kos.progs.telegram.bot.user.service;

import kos.progs.telegram.bot.common.model.Currency;
import kos.progs.telegram.bot.common.model.Lang;
import kos.progs.telegram.bot.user.model.UserInfo;
import kos.progs.telegram.bot.user.repository.memory.MemoryUserInfoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserInfoServiceTest {
    private UserInfoService userInfoService;

    @BeforeEach
    void setUp() {
        userInfoService = new UserInfoService(new MemoryUserInfoRepository());
    }

    @Test
    void testGetUserInfoDefaultRu() {
        UserInfo info = userInfoService.getUserInfo(12345L, "ru");
        assertNotNull(info);
        assertEquals(Lang.RU, info.lang());
        assertEquals(Currency.RUB, info.currency());
    }

    @Test
    void testGetUserInfoDefaultEn() {
        UserInfo info = userInfoService.getUserInfo(67890L, "de");
        assertNotNull(info);
        assertEquals(Lang.EN, info.lang());
        assertEquals(Currency.USD, info.currency());
    }

    @Test
    void testEmailValidation() {
        assertTrue(userInfoService.isValidEmail("user@example.com"));
        assertTrue(userInfoService.isValidEmail("first.last@domain.co.uk"));
        assertFalse(userInfoService.isValidEmail("invalid-email"));
        assertFalse(userInfoService.isValidEmail("@missingusername.com"));
        assertFalse(userInfoService.isValidEmail(null));
    }

    @Test
    void testPhoneValidationAndFormatting() {
        String normalized = userInfoService.normalizeAndValidatePhone("+7 (999) 123-45-67");
        assertEquals("79991234567", normalized);

        String fromEight = userInfoService.normalizeAndValidatePhone("89991234567");
        assertEquals("79991234567", fromEight);

        String formatted = userInfoService.formatPhoneNumber("79991234567");
        assertEquals("+7 (999) 123-45-67", formatted);

        assertNull(userInfoService.normalizeAndValidatePhone("123")); // too short
    }
}
