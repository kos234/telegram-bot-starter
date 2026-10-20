package kos.progs.telegram.bot.core.service;

import kos.progs.telegram.bot.button.service.ButtonService;
import kos.progs.telegram.bot.command.service.CommandService;
import kos.progs.telegram.bot.core.config.TelegramBotProperties;
import kos.progs.telegram.bot.core.config.TelegramCoreConfig;
import kos.progs.telegram.bot.states.service.StateService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.task.AsyncTaskExecutor;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BaseBotUpdaterVirtualThreadTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TelegramCoreConfig.class)
            .withBean(CommandService.class, () -> Mockito.mock(CommandService.class))
            .withBean(StateService.class, () -> Mockito.mock(StateService.class))
            .withBean(ButtonService.class, () -> Mockito.mock(ButtonService.class));

    @Test
    @DisplayName("TelegramCoreConfig defines telegramBotExecutor bean with Java 21 virtual threads")
    void testTelegramBotExecutorBeanUsesVirtualThreads() {
        contextRunner.run(context -> {
            assertThat(context).hasBean(TelegramCoreConfig.TELEGRAM_BOT_EXECUTOR_NAME);
            AsyncTaskExecutor executor = context.getBean(TelegramCoreConfig.TELEGRAM_BOT_EXECUTOR_NAME, AsyncTaskExecutor.class);

            AtomicBoolean isVirtual = new AtomicBoolean(false);
            AtomicReference<String> threadName = new AtomicReference<>();
            CountDownLatch latch = new CountDownLatch(1);

            executor.execute(() -> {
                isVirtual.set(Thread.currentThread().isVirtual());
                threadName.set(Thread.currentThread().getName());
                latch.countDown();
            });

            boolean finished = latch.await(2, TimeUnit.SECONDS);
            assertThat(finished).isTrue();
            assertThat(isVirtual.get())
                    .as("Task executed by telegramBotExecutor must run in a virtual thread")
                    .isTrue();
            assertThat(threadName.get())
                    .as("Thread name must start with 'tg-bot-vt-' prefix")
                    .startsWith("tg-bot-vt-");
        });
    }

    @Test
    @DisplayName("BaseBotUpdater.update runs asynchronously in a Java 21 virtual thread in Spring context")
    void testBaseBotUpdaterRunsInVirtualThread() {
        contextRunner.run(context -> {
            BaseBotUpdater baseBotUpdater = context.getBean(BaseBotUpdater.class);
            ButtonService buttonService = context.getBean(ButtonService.class);

            AtomicBoolean isVirtual = new AtomicBoolean(false);
            AtomicReference<String> threadName = new AtomicReference<>();
            CountDownLatch latch = new CountDownLatch(1);

            doAnswer(invocation -> {
                isVirtual.set(Thread.currentThread().isVirtual());
                threadName.set(Thread.currentThread().getName());
                latch.countDown();
                return true;
            }).when(buttonService).handle(any(CallbackQuery.class));

            Update update = mock(Update.class);
            CallbackQuery cq = mock(CallbackQuery.class);
            when(update.getCallbackQuery()).thenReturn(cq);

            // Invoke update
            baseBotUpdater.update(update);

            boolean finished = latch.await(3, TimeUnit.SECONDS);
            assertThat(finished).isTrue();
            assertThat(isVirtual.get())
                    .as("BaseBotUpdater.update must execute in a Java 21 virtual thread")
                    .isTrue();
            assertThat(threadName.get())
                    .startsWith("tg-bot-vt-");
        });
    }

    @Test
    @DisplayName("BaseBotUpdater dispatches CallbackQuery directly to ButtonService without invoking command/state services")
    void testDispatchCallbackQuery() {
        CommandService commandService = mock(CommandService.class);
        StateService stateService = mock(StateService.class);
        ButtonService buttonService = mock(ButtonService.class);

        BaseBotUpdater updater = new BaseBotUpdater(commandService, stateService, buttonService);

        Update update = mock(Update.class);
        CallbackQuery cq = mock(CallbackQuery.class);
        when(update.getCallbackQuery()).thenReturn(cq);

        updater.update(update);

        verify(buttonService, times(1)).handle(cq);
        verifyNoInteractions(commandService);
        verifyNoInteractions(stateService);
    }

    @Test
    @DisplayName("BaseBotUpdater dispatches Message command to CommandService")
    void testDispatchMessageCommand() {
        CommandService commandService = mock(CommandService.class);
        StateService stateService = mock(StateService.class);
        ButtonService buttonService = mock(ButtonService.class);

        BaseBotUpdater updater = new BaseBotUpdater(commandService, stateService, buttonService);

        Update update = mock(Update.class);
        Message message = mock(Message.class);
        when(update.getMessage()).thenReturn(message);
        when(commandService.handle(message)).thenReturn(true);

        updater.update(update);

        verify(commandService, times(1)).handle(message);
        verifyNoInteractions(stateService);
        verifyNoInteractions(buttonService);
    }

    @Test
    @DisplayName("BaseBotUpdater falls back to StateService when CommandService returns false")
    void testDispatchMessageStateFallback() {
        CommandService commandService = mock(CommandService.class);
        StateService stateService = mock(StateService.class);
        ButtonService buttonService = mock(ButtonService.class);

        BaseBotUpdater updater = new BaseBotUpdater(commandService, stateService, buttonService);

        Update update = mock(Update.class);
        Message message = mock(Message.class);
        when(update.getMessage()).thenReturn(message);
        when(commandService.handle(message)).thenReturn(false);
        when(stateService.handle(message)).thenReturn(true);

        updater.update(update);

        verify(commandService, times(1)).handle(message);
        verify(stateService, times(1)).handle(message);
        verifyNoInteractions(buttonService);
    }

    @Test
    @DisplayName("BaseBotUpdater ignores null update gracefully")
    void testNullUpdate() {
        CommandService commandService = mock(CommandService.class);
        StateService stateService = mock(StateService.class);
        ButtonService buttonService = mock(ButtonService.class);

        BaseBotUpdater updater = new BaseBotUpdater(commandService, stateService, buttonService);
        updater.update(null);

        verifyNoInteractions(commandService, stateService, buttonService);
    }
}
