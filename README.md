# Telegram Bot Spring Boot Starter

Модульный набор Spring Boot стартеров для быстрой разработки Telegram-ботов на основе официального `telegrambots-springboot-longpolling-starter` (Java 21, Spring Boot 3/4).

Проект построен по архитектуре **вертикальных срезов (Vertical Slice Architecture)**, где каждый срез представляет собой обособленный функциональный блок со своей собственной конфигурацией (`@Configuration`), хэндлерами, сервисами и репозиториями.

---

## 📦 Модули проекта

1. **`telegram-bot-spring-boot-starter`** (`kos.progs:telegram-bot-spring-boot-starter:1.0.0-SNAPSHOT`) — базовое ядро бота (диспетчеризация, роутинг, FSM, списки, локализация, меню, настройки, админка).
2. **`telegram-bot-payment-tinkoff-spring-boot-starter`** (`kos.progs:telegram-bot-payment-tinkoff-spring-boot-starter:1.0.0-SNAPSHOT`) — подключаемый модуль интернет-эквайринга Т-Банка (вебхуки, инициализация платежей, QR-коды, чеки).
3. **`telegram-bot-starter-runner`** (`kos.progs:telegram-bot-starter-runner:1.0.0-SNAPSHOT`) — модуль проверки, тестирования и запуска стартеров в Docker и локально.

---

## 🎯 Что даёт этот стартер (Уже реализованная функциональность)

Стартер берет на себя всю рутинную инфраструктуру Telegram-бота:

| Область | Что уже реализовано в стартере «из коробки» | Что реализует конкретный бот (клиентский код) |
| :--- | :--- | :--- |
| **Потоковая модель & Dispatching** | Виртуальные потоки Java 21 (`ThreadPerTaskExecutor`), неблокирующий LongPolling `BaseBotConsumer`, мгновенный `AnswerCallbackQuery` для снятия спиннера ожидания кнопок. | Только бизнес-логика в хэндлерах. |
| **Роутинг и контракты** | Маршрутизация апдейтов (`ButtonService` -> `CommandService` -> `StateService`), унифицированный результат `HandleResult` (`UNHANDLED`, `HANDLED_SUCCESS`, `HANDLED_ERROR`). | Реализация интерфейсов `CommandHandler`, `ButtonHandler`, `StateHandler`. |
| **FSM (Конечный автомат)** | Интерфейс-маркер `StateFrame` с полиморфной сериализацией `@JsonTypeInfo`, хранилища состояний на выбор: **Memory**, **Redis**, **PostgreSQL (JPA)**. Автоматическое сохранение и очистка. | Доменные классы состояний (поиск, ввод количества, заполнение формы). |
| **Стек навигации и история** | `NavigationService` с Caffeine-кэшированием, стек переходов `HistoryAction` (`ADD`, `ROLLBACK`, `IGNORE`), готовый `CancelHandler` для кнопки «Назад / Отмена». | Решение, когда вызывать откат или добавлять шаг в историю. |
| **Кнопки и аргументы** | Обход жесткого лимита Telegram в 64 байта через компактную бинарную упаковку в `ByteBuffer` и Base64-кодирование без паддинга, валидация размера в `ButtonService.buildCommand`. | Классы аргументов (`PrimitiveArgs` или `ButtonArgs`). |
| **Постраничные списки** | Абстрактная пагинация `telegram.lists`: кнопки `<`, `>`, `1/N` (`AbstractMoveButtons`, `AbstractMoveCommand`), прямой переход на страницу по номеру с валидацией ввода (`AbstractGoToPageHandler`). | Поставщик данных страницы (товары, заказы, адреса). |
| **Локализация (i18n)** | Двухуровневый `MessageSource` (`messages` перекрывает `starter_messages`), мультиязычный сервис `MessagesService` с методами `get()` и `getAll()`, переключение языка на лету. | Файлы `messages_<lang>.properties` со специфичными текстами бота. |
| **Главное меню (`/start`)** | Полноценный `StartHandler`, собирающий клавиатуру через SPI `MainMenuContributor`, автоматическая кнопка «Админка» для администраторов. | `MainMenuContributor` с кнопками вашего бота (Каталог, Корзина и т.д.). |
| **Панель админа (`/admin`)** | Полное управление сотрудниками (просмотр, добавление, удаление по Telegram ID), расширение меню через SPI `AdminMenuItemProvider`. | `AdminMenuItemProvider` с кнопками разделов админки (Товары, Категории и т.д.). |
| **Меню настроек (`/settings`)** | Готовое меню смены языка, валюты, ввода и валидации email и телефона, расширяемое через SPI `SettingsRowContributor`. | Дополнительные строки настроек (например, адреса доставки). |
| **Динамические языки и валюты** | Расширяемые реестры `Lang` и `Currency`, регистрация через свойства, Java API или Spring Beans. | Добавление специфичных для проекта валют/языков (KZT, EUR, DE, KZ). |
| **Оплата (Т-Банк)** | Вебхук-контроллер, работа с платежами, QR-кодами, чеками, события `TinkoffPaymentConfirmedEvent`. | Обработка события успешной оплаты заказа. |

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
│   └── service/            # MessagesService (чтение messages и starter_messages)
│
├── common/                 # Общие разделяемые модели
│   ├── config/             # CustomLangAndCurrencyInitializer
│   └── model/              # Lang (RU, EN, кастомные), Currency (RUB, USD, кастомные)
│
└── main_menu/              # Точки расширения главного меню
    └── MainMenuContributor.java    # Расширение меню /start
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
telegram.bot.language.enabled=true
telegram.bot.language.default-code=RU
# Регистрация дополнительных языков через свойства:
# telegram.bot.language.custom[0].id=2
# telegram.bot.language.custom[0].code=DE
# telegram.bot.language.custom[0].tag=de-DE
# telegram.bot.language.custom[0].title=Deutsch

# 5. Настройки валют (Currency)
telegram.bot.currency.enabled=true
telegram.bot.currency.default-code=RUB
# Регистрация дополнительных валют через свойства:
# telegram.bot.currency.custom[0].id=2
# telegram.bot.currency.custom[0].code=EUR
# telegram.bot.currency.custom[0].title=€

# 6. Настройки email и телефона в меню /settings
telegram.bot.email.enabled=true
telegram.bot.phone.enabled=true

# (Опционально) Настройки модуля эквайринга Т-Банка
bot.payment.tinkoff.enabled=true
bot.payment.tinkoff.terminal-key=YOUR_TERMINAL_KEY
bot.payment.tinkoff.password=YOUR_PASSWORD
bot.payment.tinkoff.server-url=https://your-domain.com
bot.payment.tinkoff.storage=jpa
```

---

## 🧭 Полный гайдлайн по разработке бота

### 1. Обработка текстовых команд и Reply-кнопок (`CommandHandler`)

Все входящие текстовые сообщения из Telegram обрабатываются реализациями интерфейса `CommandHandler`.

#### Контракт интерфейса:
```java
package kos.progs.telegram.bot.command.handler;

public interface CommandHandler {
    boolean isHandle(String command, String lastHistory);
    HandleResult handle(Message message, UserInfo userInfo, String command) throws TelegramApiException;
}
```

#### Правила именования и сопоставления (`isHandle`):
1. **Slash-команды** (`/start`, `/admin`, `/help`):
   ```java
   @Override
   public boolean isHandle(String command, String lastHistory) {
       return command.equalsIgnoreCase("/catalog");
   }
   ```
2. **ReplyKeyboard-кнопки меню** (текстовые кнопки нижней клавиатуры):
   > [!IMPORTANT]
   > Текст на кнопке отправляется на языке интерфейса пользователя (например, `"🛍 Каталог"` на русском и `"🛍 Catalog"` на английском). Никогда не делайте жесткое сравнение с одной строкой!
   > Всегда используйте `messagesService.getAll("key.title").contains(command)`, который возвращает список переводов этого ключа для **всех** зарегистрированных в боте языков:
   ```java
   @Override
   public boolean isHandle(String command, String lastHistory) {
       return messagesService.getAll("catalog.title").contains(command);
   }
   ```
3. **Использование контекста `lastHistory`**:
   Параметр `lastHistory` содержит предыдущую команду или экран пользователя из `NavigationService`. Это позволяет реагировать на один и тот же текст по-разному в зависимости от того, где находится пользователь.

#### Разбор аргументов текстовых команд:
Если команда передает параметры через пробел (например, deep-linking `/start ref_123` или `/search кроссовки`):
```java
String[] parts = command.split("\\s+", 2);
String rawCmd = parts[0];
String argument = parts.length > 1 ? parts[1].trim() : null;
```

#### Формирование результата `HandleResult`:
- `HandleResult.unhandled()` — команда не подошла, роутер продолжает опрос других хэндлеров.
- `HandleResult.success().history(HistoryAction.ADD).state(new SomeState()).build()` — команда успешно обработана, добавляется запись в стек истории и активируется FSM-состояние.
- `HandleResult.error(String message)` — ошибка обработки (цепочка прерывается, пишется в лог).

#### Реальный пример из `shop_bot` (`CatalogHandler`):
```java
@Service
@RequiredArgsConstructor
public class CatalogHandler implements CommandHandler {
    private final CategoryService categoryService;
    private final CancelHandler cancelHandler;
    private final SelectCategoryCommand selectCategoryCommand;
    private final TelegramClient telegramClient;
    private final MessagesService messagesService;

    @Override
    public boolean isHandle(String command, String lastHistory) {
        return messagesService.getAll("catalog.title").contains(command);
    }

    @Override
    public HandleResult handle(Message message, UserInfo userInfo, String command) throws TelegramApiException {
        Lang lang = userInfo.lang();
        
        List<InlineKeyboardRow> rows = new ArrayList<>();
        for (Category cat : categoryService.getCategories(lang)) {
            rows.add(new InlineKeyboardRow(
                InlineKeyboardButton.builder()
                    .text(cat.title())
                    .callbackData(selectCategoryCommand.buildCommand(cat.id()))
                    .build()
            ));
        }
        // Кнопка «Назад» формируется через готовый CancelHandler
        rows.add(new InlineKeyboardRow(cancelHandler.getInlineKeyboardButton(lang)));

        telegramClient.execute(SendMessage.builder()
                .chatId(message.getChatId())
                .text(messagesService.get("catalog.message_text", lang))
                .replyMarkup(new InlineKeyboardMarkup(rows))
                .build());

        return HandleResult.success()
                .history(HistoryAction.ADD)
                .state(ProductViewFrame.builder().categoryId(-1).build())
                .build();
    }
}
```

---

### 2. Обработка кнопок и упаковка аргументов (`ButtonHandler`, `ButtonArgs`, `ButtonService`)

При клике на `InlineKeyboardButton` Telegram отправляет `CallbackQuery`. В стартере обработка инлайн-кнопок решает две критические проблемы:
1. **Жесткий лимит Telegram в 64 байта на `callback_data`**.
2. **Зависание спиннера на кнопке и таймаут `query is too old`**.

#### Как устроен формат данных кнопки:
Формат: `<command>[=<base64_payload>]`
- `<command>` — строковый идентификатор команды (рекомендуется делать коротким, 1–10 символов, например `sel-cat`, `pnext`, `cart-add`).
- `=` — разделитель.
- `<base64_payload>` — бинарные аргументы, закодированные через Base64 без паддинга (`Base64.getEncoder().withoutPadding()`).

#### Метод `ButtonService.buildCommand`:
Никогда не склеивайте callbackData руками! Используйте `ButtonService.buildCommand`:
```java
// Без аргументов:
String data = ButtonService.buildCommand("refresh");

// С аргументами:
String data = ButtonService.buildCommand("sel-cat", new PrimitiveArgs<>(categoryId));
```
> [!IMPORTANT]
> `ButtonService.buildCommand` автоматически проверяет длину итоговой строки в байтах UTF-8. Если размер превышает 64 байта, выбрасывается `IllegalArgumentException` прямо во время формирования разметки, защищая от неожиданных ошибок Telegram API в рантайме.

#### Контракт интерфейса `ButtonHandler`:
```java
package kos.progs.telegram.bot.button.handler;

public interface ButtonHandler {
    boolean isHandle(String command);
    HandleResult handle(CallbackQuery callbackQuery, UserInfo userInfo, String command, ByteBuffer rawArgs) throws TelegramApiException;
}
```
Обратите внимание:
- В `isHandle(command)` передается чистое имя команды (до знака `=`).
- В `handle(...)` аргументы передаются уже в виде готового декодированного `ByteBuffer rawArgs`. Хэндлеру не нужно знать про Base64.
- `ButtonService` мгновенно отправляет `AnswerCallbackQuery` в Telegram **до** вызова `handle()`, поэтому спиннер у пользователя гаснет сразу даже при выполнении тяжелых операций в БД.

#### Способ 1. Упаковка простых типов (`PrimitiveArgs`):
Класс `PrimitiveArgs<T>` поддерживает: `Integer`, `Long`, `Short`, `Byte`, `Boolean`.

1. Создание сервиса-команды:
```java
@Service
public class SelectCategoryCommand {
    public static final String COMMAND = "sel-cat";

    public String buildCommand(int categoryId) {
        return ButtonService.buildCommand(COMMAND, new PrimitiveArgs<>(categoryId));
    }
}
```
2. Обработчик кнопки (`ButtonHandler`):
```java
@Service
@RequiredArgsConstructor
public class SelectCategoryButton implements ButtonHandler {
    private final TelegramProductPresenter presenter;

    @Override
    public boolean isHandle(String command) {
        return SelectCategoryCommand.COMMAND.equals(command);
    }

    @Override
    public HandleResult handle(CallbackQuery callbackQuery, UserInfo userInfo, String command, ByteBuffer rawArgs) throws TelegramApiException {
        PrimitiveArgs<Integer> args = new PrimitiveArgs<>(Integer.class);
        args.fromByteBuffer(rawArgs);
        int categoryId = args.getValue();

        return presenter.presentCategory(userInfo, callbackQuery.getMessage().getChatId(), categoryId);
    }
}
```

#### Способ 2. Упаковка сложных составных аргументов (`ButtonArgs`):
Если кнопка должна передавать несколько значений (например, `amount`, `productId`, `sizeId`), реализуйте `ButtonArgs`:

> [!TIP]
> **Почему бинарный `ByteBuffer`, а не JSON?**
> UUID в виде строки занимает 36 байт, а в binary — всего 16 байт (2 `long`). Число `1000000` в строке занимает 7 байт, а в binary — 4 байта (`int`). Упаковка в binary гарантирует, что даже 5–7 сложных полей свободно укладываются в 64-байтный лимит Telegram!

Пример из `shop_bot` (`AbstractCartAdderArgs`):
```java
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CartAdderArgs implements ButtonArgs {
    private int amount;
    private UUID productId;
    private int productSizeId;

    @Override
    public ByteBuffer toByteBuffer() {
        // Вычисляем точный размер: int (4) + UUID (16) + int (4) = 24 байта
        ByteBuffer buffer = ByteBuffer.wrap(new byte[Integer.BYTES * 2 + Long.BYTES * 2]);
        buffer.putInt(amount);
        buffer.putLong(productId.getMostSignificantBits());
        buffer.putLong(productId.getLeastSignificantBits());
        buffer.putInt(productSizeId);
        return buffer;
    }

    @Override
    public void fromByteBuffer(ByteBuffer byteBuffer) {
        this.amount = byteBuffer.getInt();
        this.productId = new UUID(byteBuffer.getLong(), byteBuffer.getLong());
        this.productSizeId = byteBuffer.getInt();
    }
}
```

---

### 3. Конечный автомат состояний FSM (`StateFrame`, `StateHandler`, `StateService`)

Конечный автомат (FSM) используется, когда бот ждет от пользователя произвольный текстовый ввод: поисковый запрос, промокод, адрес доставки, произвольное количество товара и т.д.

#### 1. Создание класса состояния (`StateFrame`):
Любое состояние должно реализовывать маркерный интерфейс `StateFrame`:
```java
package kos.progs.telegram.bot.states.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public interface StateFrame {
}
```
Благодаря аннотации `@JsonTypeInfo` полиморфная сериализация в JSON работает «из коробки» для любого хранилища (Memory, Redis, JPA).

Пример состояния поиска:
```java
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchFrame implements StateFrame {
    private int categoryId;
}
```

#### 2. Переход в состояние:
Чтобы перевести пользователя в состояние, верните его в `HandleResult` из любого `CommandHandler` или `ButtonHandler`:
```java
return HandleResult.success()
        .history(HistoryAction.ADD)
        .state(new SearchFrame(categoryId))
        .build();
```

#### 3. Обработка ввода в состоянии (`StateHandler`):
```java
@Service
@RequiredArgsConstructor
public class SearchStateHandler implements StateHandler {
    private final TelegramProductPresenter presenter;
    private final TelegramClient telegramClient;
    private final MessagesService messagesService;

    @Override
    public boolean isHandle(StateFrame frame) {
        return frame instanceof SearchFrame;
    }

    @Override
    public HandleResult handle(Message message, UserInfo userInfo, StateFrame frame) throws TelegramApiException {
        SearchFrame searchFrame = (SearchFrame) frame;
        String query = message.getText();

        if (query == null || query.isBlank()) {
            telegramClient.execute(SendMessage.builder()
                    .chatId(message.getChatId())
                    .text(messagesService.get("global.error.empty_value", userInfo.lang()))
                    .build());
            // Возвращаем unhandled или error - пользователь остается в текущем состоянии для повтора
            return HandleResult.error("Empty query");
        }

        // Выполняем поиск и отображаем каталог
        HandleResult result = presenter.presentSearch(userInfo, message.getChatId(), searchFrame.getCategoryId(), query);
        
        // Сбрасываем состояние после завершения диалога (.state(null))
        return result.withUpdateState(true).withNewState(null);
    }
}
```

---

### 4. Навигация, стек истории и кнопка «Назад / Отмена» (`NavigationService`, `CancelHandler`)

Стартер ведет стек переходов пользователя для реализации навигационной цепочки «Хлебные крошки» и кнопки возврата.

#### `HistoryAction` в `HandleResult`:
- `HistoryAction.ADD` — помещает команду или состояние в стек истории.
- `HistoryAction.ROLLBACK` — откатывает историю назад на `rollbackCount` шагов (по умолчанию 1).
- `HistoryAction.IGNORE` — оставляет историю без изменений (используется для пагинации или служебных действий).

#### Готовые кнопки возврата (`CancelHandler`):
В стартере уже зарегистрирован `CancelHandler`, который слушает команду возврата на всех языках (`messagesService.getAll("cancel.title")`):
```java
@Autowired
private CancelHandler cancelHandler;

// Кнопка для Reply-клавиатуры:
KeyboardButton replyCancel = cancelHandler.getKeyboardButton(userInfo.lang());

// Кнопка для Inline-клавиатуры:
InlineKeyboardButton inlineCancel = cancelHandler.getInlineKeyboardButton(userInfo.lang());
```

При нажатии на эту кнопку `CancelHandler` автоматически:
1. Вызывает `navigationService.goBack(chatId, 1)`.
2. Извлекает предыдущий экран или возвращает `/start`.
3. Публикует синтетический `BaseBotConsumer.NewUpdateEvent`, который повторно выполняет хэндлер предыдущего экрана, полностью восстанавливая контекст.

---

### 5. Постраничные списки и пагинация (`lists`)

Срез `kos.progs.telegram.bot.lists` предоставляет готовую инфраструктуру постраничного вывода любых коллекций (товаров, заказов, документов, пользователей) с кнопками `<`, `>`, `1/N` и вводом номера страницы.

#### Архитектура списков:
1. **Команда пагинации** (`AbstractMoveCommand`):
   Задает префикс (например, `"p"`) и автоматически генерирует подкоманды:
   - `pnext` — следующая страница
   - `pprev` — предыдущая страница
   - `pbyi` — переход на страницу по номеру
2. **Аргументы пагинации** (`AbstractMoveArgs` и `AbstractMoveToArgs`):
   Хранят базовые поля (`index`, `offset`, `startMessageId`) и предоставляют хуки для доменных данных (`extraToByteBuffer`, `extraFromByteBuffer`).
3. **Обработчик кнопок навигации** (`AbstractMoveButtons`):
   Автоматически пересчитывает индекс (`args.getIndex() + 1` или `-1`), обрабатывает кнопку выбора номера страницы и вызывает ваш абстрактный метод `moveList(...)`.
4. **Обработчик ручного ввода страницы** (`AbstractGoToPageHandler`):
   При нажатии на `pbyi` бот просит пользователя ввести номер страницы. `AbstractGoToPageHandler` валидирует ввод:
   - Проверяет, что введено число (`global.error.value_not_number`).
   - Проверяет диапазон от `1` до `maxPage` (`global.error.value_less`, `global.error.value_great`).
   - При успехе перелистывает на `page - 1` и выполняет откат истории.

#### Пример реализации пагинации из `shop_bot`:

```java
// 1. Команда пагинации товаров
@Service
public class MoveProductCommand extends AbstractMoveCommand {
    @Override
    public String getPrefix() {
        return "p";
    }
}

// 2. Обработчик кнопок пагинации товаров
@Service
public class MoveProductButton extends AbstractMoveButtons {
    private final TelegramProductPresenter presenter;

    public MoveProductButton(MessagesService messagesService, TelegramClient telegramClient,
                             CancelHandler cancelHandler, MoveProductCommand moveCommand,
                             TelegramProductPresenter presenter) {
        super(messagesService, telegramClient, cancelHandler, moveCommand);
        this.presenter = presenter;
    }

    @Override
    public AbstractMoveArgs factoryArgs() {
        return new MoveProductArgs();
    }

    @Override
    public AbstractMoveToArgs factoryByArgs() {
        return new MoveToProductArgs();
    }

    @Override
    public HandleResult moveList(Lang lang, Currency currency, long chatId, long userId, int index, int messageId, AbstractMoveArgs args) throws TelegramApiException {
        MoveProductArgs realArgs = (MoveProductArgs) args;
        return presenter.presentCatalog(lang, currency, chatId, userId, index, args.getOffset(), args.getStartMessageId(), realArgs.getCategoryId(), realArgs.getSearchId());
    }
}

// 3. Обработчик прямого перехода на страницу
@Service
public class GoToPageProductHandler extends AbstractGoToPageHandler {
    private final TelegramProductPresenter presenter;
    private final MoveProductCommand moveCommand;

    public GoToPageProductHandler(MessagesService messagesService, TelegramClient telegramClient,
                                 TelegramProductPresenter presenter, MoveProductCommand moveCommand) {
        super(messagesService, telegramClient);
        this.presenter = presenter;
        this.moveCommand = moveCommand;
    }

    @Override
    protected boolean isHandle(String prefix) {
        return prefix.equals(moveCommand.getPrefix());
    }

    @Override
    public AbstractMoveToArgs factoryByArgs() {
        return new MoveToProductArgs();
    }

    @Override
    public HandleResult moveList(Lang lang, Currency currency, long chatId, long userId, int index, int messageId, AbstractMoveToArgs args) throws TelegramApiException {
        MoveToProductArgs real = (MoveToProductArgs) args;
        return presenter.presentCatalog(lang, currency, chatId, userId, index, args.getOffset(), args.getStartMessageId(), real.getCategoryId(), real.getSearchId());
    }

    @Override
    public int getMaxPage(long userId, AbstractMoveToArgs args) {
        MoveToProductArgs real = (MoveToProductArgs) args;
        return presenter.getTotalProductPages(real.getCategoryId(), real.getSearchId());
    }
}
```

---

### 6. Локализация и интернационализация (`i18n`, `MessagesService`)

В стартере настроен двухуровневый `MessageSource` со списком баз:
1. `messages` — файлы локализации вашего проекта (`src/main/resources/messages_*.properties`).
2. `starter_messages` — встроенные системные файлы стартера (`starter_messages_ru.properties`, `starter_messages_en.properties`).

#### Переопределение системных сообщений:
Поскольку `messages` проверяется первым, вы можете переопределить **любое** стандартное сообщение стартера (например, `start.title`, `cancel.title`, `settings.title`), просто добавив ключ в свой `messages_ru.properties`:
```properties
# Переопределение текста главного меню и кнопки "Назад" в проекте shop_bot
start.title=В главное меню
cancel.title=↩ Назад
```

#### Добавление доменных сообщений:
Добавляйте любые ключи вашего бота в `src/main/resources/messages_ru.properties` и `messages_en.properties`:
```properties
catalog.title=🛍 Каталог
catalog.message_text=Выберите интересующую категорию:
cart.title=🛒 Корзина
order.title=📦 Заказы
order.payed=Заказ №{0} успешно оплачен!
```

#### Использование `MessagesService`:
Инжектируйте `MessagesService` в свои сервисы и хэндлеры:
```java
@Autowired
private MessagesService messagesService;

// Получение строки на языке пользователя с подстановкой плейсхолдеров {0}, {1}
String text = messagesService.get("order.payed", userInfo.lang(), orderId);

// Получение всех вариантов перевода ключа (для проверки Reply-кнопок в isHandle)
List<String> variants = messagesService.getAll("catalog.title");
```

---

### 7. Расширение главного меню и админки (SPI Contributor Pattern)

Вам не нужно модифицировать код стартера, чтобы добавить кнопки в `/start`, `/admin` или `/settings`.

#### 1. Расширение главного меню (`/start`):
Реализуйте интерфейс `MainMenuContributor`:
```java
@Component
public class ShopMainMenuContributor implements MainMenuContributor {
    private final MessagesService messagesService;

    public ShopMainMenuContributor(MessagesService messagesService) {
        this.messagesService = messagesService;
    }

    @Override
    public int getOrder() {
        return 10; // Чем меньше число, тем выше кнопки
    }

    @Override
    public List<KeyboardRow> getRows(long userId, Lang lang) {
        return List.of(
            new KeyboardRow(messagesService.get("catalog.title", lang), messagesService.get("cart.title", lang)),
            new KeyboardRow(messagesService.get("order.title", lang), messagesService.get("settings.title", lang))
        );
    }
}
```
Базовый `StartHandler` автоматически объединит кнопки всех контрибьюторов и добавит кнопку «Админка», если пользователь является администратором.

#### 2. Расширение панели администратора (`/admin`):
Реализуйте интерфейс `AdminMenuItemProvider`:
```java
@Component
public class CatalogAdminMenuItemProvider implements AdminMenuItemProvider {
    @Override
    public int getOrder() {
        return 10;
    }

    @Override
    public InlineKeyboardRow getButtonRow(long adminId, Lang lang) {
        return new InlineKeyboardRow(
            InlineKeyboardButton.builder()
                .text("🏷 Категории и товары")
                .callbackData("adm:catalog")
                .build()
        );
    }
}
```
Стартер всегда отображает блок управления сотрудниками («Персонал»), а кнопки остальных разделов подгружаются из ваших провайдеров.

#### 3. Расширение меню настроек (`SettingsRowContributor`):
```java
@Component
public class DeliveryAddressSettingsContributor implements SettingsRowContributor {
    @Override
    public int getOrder() {
        return 500;
    }

    @Override
    public List<InlineKeyboardRow> getRows(UserInfo userInfo) {
        return List.of(new InlineKeyboardRow(
            InlineKeyboardButton.builder()
                .text("📍 Адрес доставки")
                .callbackData("set:address")
                .build()
        ));
    }

    @Override
    public String getExtraInfo(UserInfo userInfo) {
        return "Текущий адрес: г. Москва...";
    }
}
```

---

### 8. Динамические языки (`Lang`) и валюты (`Currency`)

Языки и валюты не являются жесткими enum-константами и поддерживают динамическую регистрацию.

```properties
# Добавление Казахского языка и Тенге в application.properties
telegram.bot.language.custom[0].id=3
telegram.bot.language.custom[0].code=KZ
telegram.bot.language.custom[0].tag=kk
telegram.bot.language.custom[0].title=Қазақша

telegram.bot.currency.custom[0].id=3
telegram.bot.currency.custom[0].code=KZT
telegram.bot.currency.custom[0].title=₸
```
После регистрации:
- Язык и валюта автоматически появляются в кнопках выбора в `/settings`.
- Данные корректно сериализуются в Redis / JPA.
- Для нового языка достаточно создать файл `messages_kk.properties`.

---

### 9. Асинхронность и виртуальные потоки (Java 21 Virtual Threads)

1. **Неблокирующий LongPolling**:
   В `TelegramCoreConfig` включен `@EnableAsync` и настроен пул `telegramBotExecutor` на виртуальных потоках (`Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("tg-bot-vt-", 1).factory())`).
2. Каждый входящий `Update` обрабатывается в отдельном легковесном виртуальном потоке. Длительные запросы к базе данных, сторонним сервисам или платежным шлюзам не блокируют поток получения обновлений.
3. Мгновенное подтверждение `AnswerCallbackQuery` в `ButtonService` гарантирует, что спиннер загрузки кнопки гаснет немедленно.

---

### 10. Модуль эквайринга Т-Банка (`telegram-bot-payment-tinkoff-spring-boot-starter`)

Подключаемый модуль для приема платежей:
1. Автоматический вебхук-контроллер `TBusinessController`.
2. Создание платежных ссылок и QR-кодов через `TinkoffService`.
3. Публикация события `TinkoffPaymentConfirmedEvent` в Spring `ApplicationEventPublisher` при получении подтверждения оплаты от банка.

В клиентском боте достаточно написать слушатель:
```java
@Service
@RequiredArgsConstructor
public class PaymentEventListener {
    private final TelegramClient telegramClient;
    private final MessagesService messagesService;

    @EventListener
    public void onPaymentConfirmed(TinkoffPaymentConfirmedEvent event) {
        long userId = event.getUserId();
        // Отправка уведомления об успешной оплате
    }
}
```

---

## 🤖 Инструкция и чеклист для AI-моделей (Developer & AI Guidelines)

При генерации кода для ботов на базе этого стартера следуйте этим правилам:

1. **Не изобретайте заново то, что уже есть**:
   - ❌ Не пишите свои классы диспетчеризации апдейтов, потоков или опроса Telegram.
   - ❌ Не делайте свои таблицы состояний пользователей без необходимости — используйте `StateFrame` и `@ConditionalOnProperty telegram.bot.state-storage`.
   - ❌ Не отправляйте `AnswerCallbackQuery` вручную в `ButtonHandler` — стартер отправляет его автоматически перед вызовом хэндлера.
   - ❌ Не пишите свою реализацию кнопок «Назад» — используйте `CancelHandler` и стек `NavigationService`.

2. **Обработка команд**:
   - Для Reply-кнопок меню **всегда** проверяйте: `messagesService.getAll("key").contains(command)`.
   - Не забывайте возвращать `HandleResult.success().history(HistoryAction.ADD).build()`, если этот экран должен участвовать в навигации.

3. **Кнопки и аргументы**:
   - Держите имена команд кнопок короткими (`<= 10` символов).
   - Всегда формируйте callbackData через `ButtonService.buildCommand(...)`.
   - Для одного аргумента используйте `new PrimitiveArgs<>(val)`.
   - Для сложных структур реализуйте `ButtonArgs` с компактной бинарной записью в `ByteBuffer`.

4. **Диалоги и формы FSM**:
   - Состояния должны реализовывать `StateFrame`.
   - В конце успешной обработки диалога обязательно сбрасывайте состояние: `.state(null)`.

5. **Локализация**:
   - Никакого захардкоженного текста в коде хэндлеров. Все сообщения пользователю должны запрашиваться через `messagesService.get("key", userInfo.lang(), args...)`.

---

## 🧪 Модуль проверки и запуска: `telegram-bot-starter-runner`

Модуль `telegram-bot-starter-runner` создан специально для верификации и запуска стартеров в изолированном окружении, а также в Docker-контейнерах.

### ⚙️ Доступные параметры стартера (`application.properties`)

В модуле настроен файл `application.properties`, где активированы и задокументированы все доступные параметры стартера:

```properties
# 1. Токен бота
telegram.bot.token=${BOT_TOKEN:123456789:AAFakeTokenForTestingStarterPurposes00}

# 2. Хранилища данных
telegram.bot.state-storage=redis     # redis, memory, jpa
telegram.bot.user-storage=jpa        # jpa, memory, redis
telegram.bot.admin-storage=jpa       # jpa, memory

# 3. Поддержка языков и регистрация кастомных языков
telegram.bot.language.enabled=true
telegram.bot.language.default-code=RU
telegram.bot.language.custom[0].id=3
telegram.bot.language.custom[0].code=KZ
telegram.bot.language.custom[0].tag=kk
telegram.bot.language.custom[0].title=\u049A\u0430\u0437\u0430\u049B\u0448\u0430 # Қазақша

# 4. Поддержка валют и регистрация кастомных валют
telegram.bot.currency.enabled=true
telegram.bot.currency.default-code=RUB
telegram.bot.currency.custom[0].id=3
telegram.bot.currency.custom[0].code=KZT
telegram.bot.currency.custom[0].title=\u0422\u0435\u043D\u0433\u0435 # Тенге

# 5. Email и телефон
telegram.bot.email.enabled=true
telegram.bot.phone.enabled=true

# 6. Модуль платежей Т-Банк
bot.payment.tinkoff.enabled=true
bot.payment.tinkoff.server-url=${SERVER_URL:http://localhost:8080}
bot.payment.tinkoff.storage=jpa
```

### 🐳 Запуск в Docker Compose

Конфигурация `compose.yaml` включает сервисы PostgreSQL, Redis и сам `bot-server`:

```bash
# 1. Запуск всех сервисов (PostgreSQL, Redis, Bot Runner)
docker compose up --build

# 2. Остановка сервисов
docker compose down
```

После старта доступны endpoints проверки:
- `http://localhost:8080/health` — статус работы приложения
- `http://localhost:8080/starter/info` — сводная информация об активированных хранилищах, зарегистрированных языках и валютах

---

## 🛠 Сборка проекта

```bash
./mvnw clean install
```

Все модули собираются, запускаются юнит-тесты, и артефакты устанавливаются в локальный репозиторий Maven (`~/.m2`).
