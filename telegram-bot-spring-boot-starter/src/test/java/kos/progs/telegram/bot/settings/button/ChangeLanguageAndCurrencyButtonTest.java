package kos.progs.telegram.bot.settings.button;

import kos.progs.telegram.bot.button.model.PrimitiveArgs;
import kos.progs.telegram.bot.common.model.Currency;
import kos.progs.telegram.bot.common.model.Lang;
import kos.progs.telegram.bot.core.model.HandleResult;
import kos.progs.telegram.bot.core.model.HandleStatus;
import kos.progs.telegram.bot.settings.presenter.SettingsPresenter;
import kos.progs.telegram.bot.user.model.UserInfo;
import kos.progs.telegram.bot.user.repository.memory.MemoryUserInfoRepository;
import kos.progs.telegram.bot.user.service.UserInfoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.MaybeInaccessibleMessage;

import java.nio.ByteBuffer;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class ChangeLanguageAndCurrencyButtonTest {
    private UserInfoService userInfoService;
    private SettingsPresenter settingsPresenter;
    private ChangeLangCommand changeLangCommand;
    private ChangeCurrencyCommand changeCurrencyCommand;
    private ChangeLanguageButton changeLanguageButton;
    private ChangeCurrencyButton changeCurrencyButton;

    @BeforeEach
    void setUp() {
        Lang.reset();
        Currency.reset();

        userInfoService = new UserInfoService(new MemoryUserInfoRepository());
        settingsPresenter = Mockito.mock(SettingsPresenter.class);
        changeLangCommand = new ChangeLangCommand();
        changeCurrencyCommand = new ChangeCurrencyCommand();

        changeLanguageButton = new ChangeLanguageButton(userInfoService, settingsPresenter, changeLangCommand);
        changeCurrencyButton = new ChangeCurrencyButton(userInfoService, changeCurrencyCommand, settingsPresenter);
    }

    @AfterEach
    void tearDown() {
        Lang.reset();
        Currency.reset();
    }

    @Test
    void testChangeLanguageButtonWithCustomLang() throws Exception {
        Lang de = Lang.register((byte) 2, "DE", Locale.GERMAN, "Deutsch");

        long userId = 100L;
        userInfoService.setUserInfo(UserInfo.firstInfo(userId, Lang.RU, Currency.RUB));

        CallbackQuery query = Mockito.mock(CallbackQuery.class);
        User user = Mockito.mock(User.class);
        Mockito.when(user.getId()).thenReturn(userId);
        Mockito.when(query.getFrom()).thenReturn(user);

        MaybeInaccessibleMessage msg = Mockito.mock(MaybeInaccessibleMessage.class);
        Mockito.when(msg.getMessageId()).thenReturn(42);
        Mockito.when(msg.getChatId()).thenReturn(999L);
        Mockito.when(query.getMessage()).thenReturn(msg);

        Mockito.when(settingsPresenter.presentSettings(Mockito.any(), Mockito.eq(999L), Mockito.eq(42)))
                .thenReturn(HandleResult.success().build());

        PrimitiveArgs<Byte> args = new PrimitiveArgs<>(de.getId());
        ByteBuffer buffer = ByteBuffer.wrap(args.toByteBuffer().array());

        UserInfo userInfo = userInfoService.getUserInfo(userId, "ru");
        HandleResult result = changeLanguageButton.handle(query, userInfo, changeLangCommand.getBaseCommand(), buffer);

        assertEquals(HandleStatus.HANDLED_SUCCESS, result.getStatus());
        UserInfo updated = userInfoService.getUserInfo(userId, "ru");
        assertEquals(de, updated.lang());
    }

    @Test
    void testChangeCurrencyButtonWithCustomCurrency() throws Exception {
        Currency eur = Currency.register((byte) 2, "EUR", "€");

        long userId = 200L;
        userInfoService.setUserInfo(UserInfo.firstInfo(userId, Lang.RU, Currency.RUB));

        CallbackQuery query = Mockito.mock(CallbackQuery.class);
        User user = Mockito.mock(User.class);
        Mockito.when(user.getId()).thenReturn(userId);
        Mockito.when(query.getFrom()).thenReturn(user);

        MaybeInaccessibleMessage msg = Mockito.mock(MaybeInaccessibleMessage.class);
        Mockito.when(msg.getMessageId()).thenReturn(43);
        Mockito.when(msg.getChatId()).thenReturn(999L);
        Mockito.when(query.getMessage()).thenReturn(msg);

        Mockito.when(settingsPresenter.presentSettings(Mockito.any(), Mockito.eq(999L), Mockito.eq(43)))
                .thenReturn(HandleResult.success().build());

        PrimitiveArgs<Byte> args = new PrimitiveArgs<>(eur.getId());
        ByteBuffer buffer = ByteBuffer.wrap(args.toByteBuffer().array());

        UserInfo userInfo = userInfoService.getUserInfo(userId, "ru");
        HandleResult result = changeCurrencyButton.handle(query, userInfo, changeCurrencyCommand.getBaseCommand(), buffer);

        assertEquals(HandleStatus.HANDLED_SUCCESS, result.getStatus());
        UserInfo updated = userInfoService.getUserInfo(userId, "ru");
        assertEquals(eur, updated.currency());
    }

    @Test
    void testSameLanguageIgnored() throws Exception {
        long userId = 300L;
        userInfoService.setUserInfo(UserInfo.firstInfo(userId, Lang.RU, Currency.RUB));

        CallbackQuery query = Mockito.mock(CallbackQuery.class);
        User user = Mockito.mock(User.class);
        Mockito.when(user.getId()).thenReturn(userId);
        Mockito.when(query.getFrom()).thenReturn(user);

        PrimitiveArgs<Byte> args = new PrimitiveArgs<>(Lang.RU.getId());
        ByteBuffer buffer = ByteBuffer.wrap(args.toByteBuffer().array());

        UserInfo userInfo = userInfoService.getUserInfo(userId, "ru");
        HandleResult result = changeLanguageButton.handle(query, userInfo, changeLangCommand.getBaseCommand(), buffer);

        assertEquals(HandleStatus.UNHANDLED, result.getStatus());
    }
}
