package kos.progs.telegram.bot.settings.presenter;

import kos.progs.telegram.bot.command.handler.CancelHandler;
import kos.progs.telegram.bot.common.model.Currency;
import kos.progs.telegram.bot.common.model.Lang;
import kos.progs.telegram.bot.i18n.service.MessagesService;
import kos.progs.telegram.bot.settings.button.ChangeCurrencyCommand;
import kos.progs.telegram.bot.settings.button.ChangeLangCommand;
import kos.progs.telegram.bot.settings.button.ChangeUserInfoCommand;
import kos.progs.telegram.bot.settings.spi.CurrencySettingsRowContributor;
import kos.progs.telegram.bot.settings.spi.EmailSettingsRowContributor;
import kos.progs.telegram.bot.settings.spi.LanguageSettingsRowContributor;
import kos.progs.telegram.bot.settings.spi.PhoneSettingsRowContributor;
import kos.progs.telegram.bot.settings.spi.SettingsRowContributor;
import kos.progs.telegram.bot.user.model.UserInfo;
import kos.progs.telegram.bot.user.repository.memory.MemoryUserInfoRepository;
import kos.progs.telegram.bot.user.service.UserInfoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class SettingsPresenterAndContributorsTest {
    private MessagesService messagesService;
    private TelegramClient telegramClient;
    private CancelHandler cancelHandler;
    private UserInfoService userInfoService;
    private ChangeUserInfoCommand changeUserInfoCommand;
    private ChangeLangCommand changeLangCommand;
    private ChangeCurrencyCommand changeCurrencyCommand;

    @BeforeEach
    void setUp() {
        Lang.reset();
        Currency.reset();

        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames("starter_messages");
        messageSource.setDefaultEncoding("UTF-8");
        messagesService = new MessagesService(messageSource);

        telegramClient = Mockito.mock(TelegramClient.class);
        cancelHandler = Mockito.mock(CancelHandler.class);
        Mockito.when(cancelHandler.getInlineKeyboardButton(Mockito.any()))
                .thenReturn(InlineKeyboardButton.builder().text("⬅Назад").callbackData("cancel").build());

        userInfoService = new UserInfoService(new MemoryUserInfoRepository());
        changeUserInfoCommand = new ChangeUserInfoCommand();
        changeLangCommand = new ChangeLangCommand();
        changeCurrencyCommand = new ChangeCurrencyCommand();
    }

    @AfterEach
    void tearDown() {
        Lang.reset();
        Currency.reset();
    }

    @Test
    void testContributorRowsWithCustomLangAndCurrency() {
        Lang de = Lang.register((byte) 2, "DE", Locale.GERMAN, "Deutsch");
        Currency eur = Currency.register((byte) 2, "EUR", "€");

        LanguageSettingsRowContributor langContributor =
                new LanguageSettingsRowContributor(messagesService, changeLangCommand);
        CurrencySettingsRowContributor currencyContributor =
                new CurrencySettingsRowContributor(messagesService, changeCurrencyCommand);

        UserInfo userInfo = new UserInfo(10L, de, eur, "test@mail.com", "79991234567");

        List<InlineKeyboardRow> langRows = langContributor.getRows(userInfo);
        assertTrue(langRows.size() >= 2);
        boolean containsDe = langRows.stream()
                .flatMap(row -> row.stream())
                .anyMatch(btn -> "Deutsch".equals(btn.getText()));
        assertTrue(containsDe, "Keyboard should contain button for custom language DE");

        List<InlineKeyboardRow> currencyRows = currencyContributor.getRows(userInfo);
        assertTrue(currencyRows.size() >= 2);
        boolean containsEur = currencyRows.stream()
                .flatMap(row -> row.stream())
                .anyMatch(btn -> "€".equals(btn.getText()));
        assertTrue(containsEur, "Keyboard should contain button for custom currency EUR");
    }

    @Test
    void testSettingsPresenterWithAllContributors() {
        LanguageSettingsRowContributor langContributor =
                new LanguageSettingsRowContributor(messagesService, changeLangCommand);
        CurrencySettingsRowContributor currencyContributor =
                new CurrencySettingsRowContributor(messagesService, changeCurrencyCommand);
        EmailSettingsRowContributor emailContributor =
                new EmailSettingsRowContributor(messagesService, changeUserInfoCommand);
        PhoneSettingsRowContributor phoneContributor =
                new PhoneSettingsRowContributor(messagesService, changeUserInfoCommand, userInfoService);

        List<SettingsRowContributor> contributors = List.of(langContributor, currencyContributor, emailContributor, phoneContributor);
        SettingsPresenter presenter = new SettingsPresenter(
                messagesService,
                telegramClient,
                cancelHandler,
                contributors
        );

        UserInfo userInfo = UserInfo.firstInfo(1L, Lang.RU, Currency.RUB);
        InlineKeyboardMarkup keyboard = presenter.getKeyboard(userInfo);

        assertNotNull(keyboard);
        List<InlineKeyboardRow> rows = keyboard.getKeyboard();

        boolean hasRu = rows.stream().flatMap(r -> r.stream()).anyMatch(b -> "Русский".equals(b.getText()));
        boolean hasRub = rows.stream().flatMap(r -> r.stream()).anyMatch(b -> "₽".equals(b.getText()));
        boolean hasEmail = rows.stream().flatMap(r -> r.stream()).anyMatch(b -> b.getCallbackData().contains("email"));
        boolean hasPhone = rows.stream().flatMap(r -> r.stream()).anyMatch(b -> b.getCallbackData().contains("phone"));
        boolean hasBack = rows.stream().flatMap(r -> r.stream()).anyMatch(b -> "⬅Назад".equals(b.getText()));

        assertTrue(hasRu);
        assertTrue(hasRub);
        assertTrue(hasEmail);
        assertTrue(hasPhone);
        assertTrue(hasBack);
    }

    @Test
    void testSettingsPresenterWhenLanguageDisabled() {
        CurrencySettingsRowContributor currencyContributor =
                new CurrencySettingsRowContributor(messagesService, changeCurrencyCommand);
        EmailSettingsRowContributor emailContributor =
                new EmailSettingsRowContributor(messagesService, changeUserInfoCommand);
        PhoneSettingsRowContributor phoneContributor =
                new PhoneSettingsRowContributor(messagesService, changeUserInfoCommand, userInfoService);

        SettingsPresenter presenter = new SettingsPresenter(
                messagesService,
                telegramClient,
                cancelHandler,
                List.of(currencyContributor, emailContributor, phoneContributor)
        );

        UserInfo userInfo = UserInfo.firstInfo(1L, Lang.RU, Currency.RUB);
        InlineKeyboardMarkup keyboard = presenter.getKeyboard(userInfo);

        boolean hasLang = keyboard.getKeyboard().stream()
                .flatMap(r -> r.stream())
                .anyMatch(b -> b.getCallbackData().contains("lang"));
        assertFalse(hasLang, "When language is disabled, no language buttons should be present");

        boolean hasRub = keyboard.getKeyboard().stream()
                .flatMap(r -> r.stream())
                .anyMatch(b -> "₽".equals(b.getText()));
        assertTrue(hasRub, "Currency buttons should still be present");
    }

    @Test
    void testSettingsPresenterWhenCurrencyDisabled() {
        LanguageSettingsRowContributor langContributor =
                new LanguageSettingsRowContributor(messagesService, changeLangCommand);
        EmailSettingsRowContributor emailContributor =
                new EmailSettingsRowContributor(messagesService, changeUserInfoCommand);
        PhoneSettingsRowContributor phoneContributor =
                new PhoneSettingsRowContributor(messagesService, changeUserInfoCommand, userInfoService);

        SettingsPresenter presenter = new SettingsPresenter(
                messagesService,
                telegramClient,
                cancelHandler,
                List.of(langContributor, emailContributor, phoneContributor)
        );

        UserInfo userInfo = UserInfo.firstInfo(1L, Lang.RU, null);
        InlineKeyboardMarkup keyboard = presenter.getKeyboard(userInfo);

        boolean hasCurrency = keyboard.getKeyboard().stream()
                .flatMap(r -> r.stream())
                .anyMatch(b -> b.getCallbackData().contains("currency"));
        assertFalse(hasCurrency, "When currency is disabled, no currency buttons should be present");

        boolean hasLang = keyboard.getKeyboard().stream()
                .flatMap(r -> r.stream())
                .anyMatch(b -> b.getCallbackData().contains("lang"));
        assertTrue(hasLang, "Language buttons should still be present");
    }

    @Test
    void testSettingsPresenterWhenEmailAndPhoneDisabled() throws Exception {
        LanguageSettingsRowContributor langContributor =
                new LanguageSettingsRowContributor(messagesService, changeLangCommand);
        CurrencySettingsRowContributor currencyContributor =
                new CurrencySettingsRowContributor(messagesService, changeCurrencyCommand);

        SettingsPresenter presenter = new SettingsPresenter(
                messagesService,
                telegramClient,
                cancelHandler,
                List.of(langContributor, currencyContributor)
        );

        UserInfo userInfo = UserInfo.firstInfo(1L, Lang.RU, Currency.RUB);
        InlineKeyboardMarkup keyboard = presenter.getKeyboard(userInfo);

        boolean hasEmail = keyboard.getKeyboard().stream()
                .flatMap(r -> r.stream())
                .anyMatch(b -> b.getCallbackData().contains("email"));
        boolean hasPhone = keyboard.getKeyboard().stream()
                .flatMap(r -> r.stream())
                .anyMatch(b -> b.getCallbackData().contains("phone"));

        assertFalse(hasEmail, "Email button should be absent when Email contributor is disabled");
        assertFalse(hasPhone, "Phone button should be absent when Phone contributor is disabled");

        Method getMessageMethod = SettingsPresenter.class.getDeclaredMethod("getMessage", UserInfo.class);
        getMessageMethod.setAccessible(true);
        String messageText = (String) getMessageMethod.invoke(presenter, userInfo);

        assertFalse(messageText.contains("Почта:"));
        assertFalse(messageText.contains("Телефон:"));
        assertTrue(messageText.contains("Язык:"));
        assertTrue(messageText.contains("Валюта:"));
    }

    @Test
    void testCustomContributorOrdering() {
        // Custom contributor with order 250 (between Currency at 200 and Email at 300)
        SettingsRowContributor deliveryContributor = new SettingsRowContributor() {
            @Override
            public int getOrder() {
                return 250;
            }

            @Override
            public List<InlineKeyboardRow> getRows(UserInfo userInfo) {
                return List.of(new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text("Адрес").callbackData("address").build()
                ));
            }

            @Override
            public String getExtraInfo(UserInfo userInfo) {
                return "Адрес: Москва";
            }
        };

        LanguageSettingsRowContributor langContributor =
                new LanguageSettingsRowContributor(messagesService, changeLangCommand);
        CurrencySettingsRowContributor currencyContributor =
                new CurrencySettingsRowContributor(messagesService, changeCurrencyCommand);
        EmailSettingsRowContributor emailContributor =
                new EmailSettingsRowContributor(messagesService, changeUserInfoCommand);

        SettingsPresenter presenter = new SettingsPresenter(
                messagesService,
                telegramClient,
                cancelHandler,
                List.of(langContributor, currencyContributor, deliveryContributor, emailContributor)
        );

        UserInfo userInfo = UserInfo.firstInfo(1L, Lang.RU, Currency.RUB);
        InlineKeyboardMarkup keyboard = presenter.getKeyboard(userInfo);

        // Find index of rows
        List<InlineKeyboardRow> rows = keyboard.getKeyboard();
        int addressIndex = -1;
        int emailIndex = -1;
        for (int i = 0; i < rows.size(); i++) {
            if (rows.get(i).stream().anyMatch(b -> "Адрес".equals(b.getText()))) addressIndex = i;
            if (rows.get(i).stream().anyMatch(b -> b.getCallbackData().contains("email"))) emailIndex = i;
        }

        assertTrue(addressIndex != -1 && emailIndex != -1);
        assertTrue(addressIndex < emailIndex, "Order 250 (Address) should precede Order 300 (Email)");
    }
}
