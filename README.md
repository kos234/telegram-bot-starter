# Telegram Bot Spring Boot Starter

Модульный набор Spring Boot стартеров для быстрой разработки Telegram-ботов на основе официального `telegrambots-springboot-longpolling-starter` (Java 21, Spring Boot 3/4).

Проект построен по архитектуре **вертикальных срезов (Vertical Slice Architecture)**, где каждый срез представляет собой обособленный функциональный блок со своей собственной конфигурацией (`@Configuration`), хэндлерами, сервисами и репозиториями.

---

## 📦 Модули проекта

1. **`telegram-bot-spring-boot-starter`** (`kos.progs:telegram-bot-spring-boot-starter:1.0.0-SNAPSHOT`)
2. **`telegram-bot-payment-tinkoff-spring-boot-starter`** (`kos.progs:telegram-bot-payment-tinkoff-spring-boot-starter:1.0.0-SNAPSHOT`)

---

## 🏗 Архитектура вертикальных срезов (`telegram-bot-spring-boot-starter`)

```
kos.progs.telegram.bot
│
├── core/                   # Ядро диспетчеризации и LongPolling
│   ├── config/             # TelegramCoreConfig, TelegramBotProperties
│   ├── model/              # HandleResult, HandleStatus
│   └── service/            # BaseBot, BaseBotConsumer, BaseBotUpdater
│
├── button/                 # Срез обработки Callback-кнопок
│   ├── config/             # ButtonConfig
│   ├── handler/            # ButtonHandler
│   ├── model/              # ButtonArgs, EmptyArgs, PrimitiveArgs
│   └── service/            # ButtonService (парсинг Base64 аргументов, проверка лимита 64 байт)
│
├── command/                # Срез обработки текстовых команд
│   ├── config/             # CommandConfig
│   ├── handler/            # CommandHandler, StartHandler, CancelHandler
│   └── service/            # CommandService
│
├── states/                 # Срез конечного автомата (FSM) и стека истории
│   ├── config/             # StateConfig (автоконфигурация Memory / Redis / JPA)
│   ├── handler/            # StateHandler
│   ├── model/              # StateFrame, HistoryAction, MessageWithState
│   ├── repository/         # StateRepository, HistoryRepository (+ memory, redis, jpa)
│   └── service/            # StateService, NavigationService
│
├── user/                   # Срез пользователей
│   ├── config/             # UserConfig (автоконфигурация Memory / Redis / JPA)
│   ├── model/              # UserInfo (record: userId, lang, currency, email, phone)
│   ├── repository/         # IUserInfoRepository (+ memory, redis, jpa)
│   └── service/            # UserInfoService (нормализация телефонов, валидация email)
│
├── admin/                  # Срез панели администратора и управления сотрудниками
│   ├── config/             # AdminConfig (автоконфигурация Memory / JPA)
│   ├── button/             # AdminsCommand, AdminsManipulateButton, AdminsViewListButton
│   ├── command/            # AdminPanelCommand (/admin)
│   ├── model/              # AdminsManipulateFrame
│   ├── presenter/          # TelegramAdminsPresenter
│   ├── repository/         # AdminsRepository (+ memory, jpa)
│   ├── service/            # AdminService, LogMetrics
│   └── state/              # AdminsManipulateState
│
├── settings/               # Срез меню настроек
│   ├── config/             # SettingsConfig
│   ├── button/             # Кнопки смены языка, валюты, телефона, email
│   ├── command/            # SettingsHandler (/settings)
│   ├── model/              # UserInfoSetState
│   ├── presenter/          # SettingsPresenter
│   ├── spi/                # SettingsRowContributor, LanguageSettingsRowContributor, CurrencySettingsRowContributor
│   └── state/              # UserInfoChangeStateHandler
│
├── lists/                  # Срез универсальных списков и пагинации
│   ├── config/             # ListConfig
│   ├── button/             # AbstractMoveButtons, AbstractMoveCommand
│   ├── model/              # AbstractMoveArgs, AbstractMoveToArgs, Page, ListAns
│   └── states/             # AbstractGoToPageHandler, GoToPageState
│
├── i18n/                   # Срез локализации и интернационализации
│   ├── config/             # I18nConfig
│   └── service/            # MessagesService (чтение starter_messages и messages)
│
├── common/                 # Общие разделяемые модели
│   ├── config/             # CustomLangAndCurrencyInitializer
│   └── model/              # Lang (RU, EN, кастомные), Currency (RUB, USD, кастомные)
│
└── spi/                    # Глобальные точки расширения (Contributor Pattern)
    ├── MainMenuContributor.java    # Расширение меню /start
    └── AdminMenuItemProvider.java  # Расширение инлайн-панели /admin
```

---

## 🚀 Быстрый старт

### 1. Подключение зависимостей в Maven

```xml
<dependencies>
    <!-- Базовый стартер бота -->
    <dependency>
        <groupId>kos.progs</groupId>
        <artifactId>telegram-bot-spring-boot-starter</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>

    <!-- (Опционально) Модуль эквайринга Т-Банка -->
    <dependency>
        <groupId>kos.progs</groupId>
        <artifactId>telegram-bot-payment-tinkoff-spring-boot-starter</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
</dependencies>
```

### 2. Параметры конфигурации (`application.properties`)

Все указанные свойства реально считываются стартером и управляют созданием бинов:

```properties
# Токен бота (проверяются: telegram.bot.token, bot.token, bot_token, BOT_TOKEN)
telegram.bot.token=YOUR_TELEGRAM_BOT_TOKEN

# 1. Хранилище FSM-состояний и истории навигации: redis (по умолчанию), memory, jpa
telegram.bot.state-storage=redis

# 2. Хранилище данных пользователей (UserInfo): jpa (по умолчанию), memory, redis
telegram.bot.user-storage=jpa

# 3. Хранилище списка администраторов: jpa (по умолчанию), memory
telegram.bot.admin-storage=jpa

# 4. Настройки языков (Lang)
# Включение возможности смены языка пользователем (по умолчанию true)
telegram.bot.language.enabled=true
# Язык по умолчанию (по умолчанию RU)
telegram.bot.language.default-code=RU
# Регистрация дополнительных языков через свойства:
# telegram.bot.language.custom[0].id=2
# telegram.bot.language.custom[0].code=DE
# telegram.bot.language.custom[0].tag=de-DE
# telegram.bot.language.custom[0].title=Deutsch

# 5. Настройки валют (Currency)
# Включение поддержки валют и возможности их смены (по умолчанию true)
telegram.bot.currency.enabled=true
# Валюта по умолчанию (по умолчанию RUB)
telegram.bot.currency.default-code=RUB
# Регистрация дополнительных валют через свойства:
# telegram.bot.currency.custom[0].id=2
# telegram.bot.currency.custom[0].code=EUR
# telegram.bot.currency.custom[0].title=€

# 6. Настройки email и телефона в меню /settings
# Включение строки и кнопки изменения email (по умолчанию true)
telegram.bot.email.enabled=true
# Включение строки и кнопки изменения телефона (по умолчанию true)
telegram.bot.phone.enabled=true

# (Опционально) Настройки модуля эквайринга Т-Банка
bot.payment.tinkoff.enabled=true
bot.payment.tinkoff.terminal-key=YOUR_TERMINAL_KEY
bot.payment.tinkoff.password=YOUR_PASSWORD
bot.payment.tinkoff.server-url=https://your-domain.com
# Хранилище платежей: memory (по умолчанию), redis, jpa
bot.payment.tinkoff.storage=memory
```

> [!NOTE]
> Авторизация администраторов защищена проверкой `AdminService.isUserAdmin(telegramId)`, включающей внутреннюю валидацию разработчика и репозиторий `AdminsRepository`. Никаких статических списков `adminIds` в конфиге не требуется.

---

## 🧩 Ключевые концепции и расширение (SPI)

### 1. Контракт хэндлеров и `HandleResult`

Все обработчики (`CommandHandler`, `ButtonHandler`, `StateHandler`) объявляют `throws TelegramApiException` и возвращают результат с одним из 3 статусов:

- `HandleResult.unhandled()` — сообщение не предназначено этому обработчику, роутер продолжает цепочку.
- `HandleResult.success()` — сообщение обработано успешно, цепочка завершается.
- `HandleResult.error(String message)` — ошибка при обработке, цепочка прерывается.

### 2. Добавление кнопок в главное меню (`/start`)

Создайте бин, реализующий `MainMenuContributor`:

```java
@Component
public class ShopMainMenuContributor implements MainMenuContributor {
    @Override
    public int getOrder() {
        return 10;
    }

    @Override
    public List<KeyboardRow> getRows(long userId, Lang lang) {
        KeyboardRow row1 = new KeyboardRow();
        row1.add("🛍 Каталог");
        row1.add("🛒 Корзина");
        
        KeyboardRow row2 = new KeyboardRow();
        row2.add("📦 Заказы");
        return List.of(row1, row2);
    }
}
```

Базовый `StartHandler` автоматически соберет строки всех контрибьюторов и добавит кнопку «Админка», если пользователь обладает правами администратора.

### 3. Расширение админ-панели (`/admin`)

Создайте бины, реализующие `AdminMenuItemProvider`:

```java
@Component
public class CatalogAdminMenuItemProvider implements AdminMenuItemProvider {
    @Override
    public int getOrder() {
        return 10;
    }

    @Override
    public InlineKeyboardRow getButtonRow(long adminId, Lang lang) {
        InlineKeyboardButton btn = InlineKeyboardButton.builder()
                .text("Категории и товары")
                .callbackData("admin:catalog")
                .build();
        return new InlineKeyboardRow(btn);
    }
}
```

### 4. Расширение настроек (`SettingsRowContributor`)

Для добавления строк настроек специфичных для домена (например, адрес доставки):

```java
@Component
public class DeliveryAddressSettingsContributor implements SettingsRowContributor {
    @Override
    public int getOrder() {
        return 20;
    }

    @Override
    public List<InlineKeyboardRow> getRows(UserInfo userInfo) {
        InlineKeyboardButton btn = InlineKeyboardButton.builder()
                .text("📍 Адрес доставки")
                .callbackData("settings:delivery_address")
                .build();
        return List.of(new InlineKeyboardRow(btn));
    }

    @Override
    public String getExtraInfo(UserInfo userInfo) {
        return "Адрес доставки: ...";
    }
}
```

### 5. Добавление кастомных языков (`Lang`) и валют (`Currency`)

Языки (`Lang`) и валюты (`Currency`) больше не являются закрытыми перечислениями и поддерживают динамическую регистрацию.

**Способ 1. Через Java API:**
```java
// Регистрация нового языка
Lang de = Lang.register((byte) 2, "DE", Locale.GERMAN, "Deutsch");

// Регистрация новой валюты
Currency eur = Currency.register((byte) 2, "EUR", "€");
```

**Способ 2. Объявление Spring-бинов:**
```java
@Configuration
public class CustomBotConfig {
    @Bean
    public Lang germanLang() {
        return new Lang((byte) 2, "DE", Locale.GERMAN, "Deutsch");
    }

    @Bean
    public Currency euroCurrency() {
        return new Currency((byte) 2, "EUR", "€");
    }
}
```

**Способ 3. Через `application.properties`:**
```properties
telegram.bot.language.custom[0].id=2
telegram.bot.language.custom[0].code=DE
telegram.bot.language.custom[0].tag=de-DE
telegram.bot.language.custom[0].title=Deutsch

telegram.bot.currency.custom[0].id=2
telegram.bot.currency.custom[0].code=EUR
telegram.bot.currency.custom[0].title=€
```

Все зарегистрированные языки и валюты автоматически появляются в меню настроек `/settings` и сериализуются в Redis / JPA.

### 6. Включение, отключение и кастомизация смены языка/валюты

Управлять возможностью смены языка и валюты можно через свойства или интерфейсы SPI:

- **Отключение через конфигурацию:**
  ```properties
  # Отключение смены языка и установка языка по умолчанию
  telegram.bot.language.enabled=false
  telegram.bot.language.default-code=RU

  # Отключение валют
  telegram.bot.currency.enabled=false
  ```
  При `enabled=false` кнопки выбора и соответствующие информационные строки в `/settings` автоматически скрываются, а обработчики кнопок отключаются.

- **Собственная логика отображения через SPI:**
  Все строки меню `/settings` (язык, валюта, email, телефон и кастомные строки) построены исключительно на интерфейсе `SettingsRowContributor` и отображаются строго по `getOrder()`:
  - `LanguageSettingsRowContributor` (order = 100) — выбор языка (`telegram.bot.language.enabled`)
  - `CurrencySettingsRowContributor` (order = 200) — выбор валюты (`telegram.bot.currency.enabled`)
  - `EmailSettingsRowContributor` (order = 300) — изменение email (`telegram.bot.email.enabled`)
  - `PhoneSettingsRowContributor` (order = 400) — изменение телефона (`telegram.bot.phone.enabled`)
  - Кастомные контрибьюторы (любой `order`) — встраиваются в меню в точности по своему порядку.

  Вы можете переопределить стандартное поведение, объявив свой бин или отнаследовавшись:
  ```java
  @Component
  public class CustomLanguageSettingsContributor extends LanguageSettingsRowContributor {
      public CustomLanguageSettingsContributor(MessagesService messagesService, ChangeLangCommand changeLangCommand) {
          super(messagesService, changeLangCommand);
      }

      @Override
      public List<InlineKeyboardRow> getRows(UserInfo userInfo) {
          // Ваша кастомная верстка кнопок выбора языка
          return super.getRows(userInfo);
      }

      @Override
      public String getExtraInfo(UserInfo userInfo) {
          return "Выбранный язык: " + userInfo.lang().getTitle();
      }
  }
  ```

### 7. Определение языка и валюты пользователя по умолчанию

Для кастомизации логики метода `UserInfoService.getUserInfo(long userId, String systemLang)` предусмотрены две возможности:

1. **Реализация интерфейса `DefaultUserInfoResolver` (рекомендуется):**
   ```java
   @Component
   public class MyDefaultUserInfoResolver implements DefaultUserInfoResolver {
       @Override
       public UserInfo createDefaultUserInfo(long userId, String systemLang) {
           if ("de".equalsIgnoreCase(systemLang)) {
               return UserInfo.firstInfo(userId, Lang.valueOf("DE"), Currency.valueOf("EUR"));
           }
           return UserInfo.firstInfo(userId, Lang.RU, Currency.RUB);
       }
   }
   ```
2. **Наследование от `UserInfoService`:**
   Класс `UserInfoService` открыт для расширения — вы можете переопределить метод `protected UserInfo createDefaultUserInfo(long userId, String systemLang)`.

### 8. Асинхронная обработка и виртуальные потоки (Java 21 Virtual Threads)

Вся обработка входящих апдейтов Telegram в стартере оптимизирована для работы с **виртуальными потоками Java 21**:

1. **`BaseBotUpdater.update` в виртуальных потоках:**
   - Аннотирован `@Async(TelegramCoreConfig.TELEGRAM_BOT_EXECUTOR_NAME)`.
   - В автоконфигурации `TelegramCoreConfig` автоматически активирован `@EnableAsync` и зарегистрирован бин `telegramBotExecutor` (`AsyncTaskExecutor`), использующий `Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("tg-bot-vt-", 1).factory())`.
   - Поток LongPolling-а не блокируется при получении апдейтов: каждый `Update` немедленно делегируется в индивидуальный виртуальный поток.
   - При необходимости исполнитель можно переопределить в своём приложении, объявив бин с именем `telegramBotExecutor`.

2. **Немедленное подтверждение кнопок (`AnswerCallbackQuery`):**
   - При нажатии на кнопку (`CallbackQuery`) `ButtonService` мгновенно отправляет `AnswerCallbackQuery` в Telegram **до** выполнения бизнес-логики хэндлера `ButtonHandler.handle(...)`.
   - Это гарантирует, что спиннер загрузки на кнопке у пользователя в Telegram гаснет сразу, а длительные блокирующие операции (запросы к БД, внешним API, платежным системам) в виртуальном потоке не приводят к ошибкам таймаута Telegram API (`query is too old`).
   - Обработка аргументов кнопок потокобезопасна при параллельном исполнении в виртуальных потоках.

---

## 🛠 Сборка проекта

```bash
./mvnw clean install
```

Все модули собираются, запускаются юнит-тесты, и артефакты устанавливаются в локальный репозиторий Maven (`~/.m2`).
