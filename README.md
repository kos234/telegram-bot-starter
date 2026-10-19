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
│   ├── spi/                # SettingsRowContributor
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
├── common/                 # Общие разделяемые энумы
│   └── model/              # Lang (RU, EN), Currency (RUB, USD)
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

# 1. Хранилище FSM-состояний и истории навигации: memory (по умолчанию), redis, jpa
telegram.bot.state-storage=memory

# 2. Хранилище данных пользователей (UserInfo): memory (по умолчанию), redis, jpa
telegram.bot.user-storage=memory

# 3. Хранилище списка администраторов: memory (по умолчанию), jpa
telegram.bot.admin-storage=memory

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

---

## 🛠 Сборка проекта

```bash
./mvnw clean install
```

Все модули собираются, запускаются юнит-тесты, и артефакты устанавливаются в локальный репозиторий Maven (`~/.m2`).
