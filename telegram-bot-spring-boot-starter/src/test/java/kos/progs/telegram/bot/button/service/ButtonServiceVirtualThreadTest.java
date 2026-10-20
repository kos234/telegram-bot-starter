package kos.progs.telegram.bot.button.service;

import kos.progs.telegram.bot.button.handler.ButtonHandler;
import kos.progs.telegram.bot.core.model.HandleResult;
import kos.progs.telegram.bot.states.model.HistoryAction;
import kos.progs.telegram.bot.states.service.NavigationService;
import kos.progs.telegram.bot.states.service.StateService;
import kos.progs.telegram.bot.user.model.UserInfo;
import kos.progs.telegram.bot.user.service.UserInfoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ButtonServiceVirtualThreadTest {

    private UserInfoService userInfoService;
    private StateService stateService;
    private NavigationService navigationService;
    private TelegramClient telegramClient;
    private ButtonHandler testHandler;
    private List<ButtonHandler> buttonHandlers;
    private ButtonService buttonService;

    @BeforeEach
    void setUp() {
        userInfoService = mock(UserInfoService.class);
        stateService = mock(StateService.class);
        navigationService = mock(NavigationService.class);
        telegramClient = mock(TelegramClient.class);
        testHandler = mock(ButtonHandler.class);

        buttonHandlers = new ArrayList<>();
        buttonHandlers.add(testHandler);

        buttonService = new ButtonService(
                buttonHandlers,
                userInfoService,
                stateService,
                navigationService,
                telegramClient
        );
    }

    private CallbackQuery createMockCallbackQuery(String id, String data, long userId) {
        CallbackQuery cq = mock(CallbackQuery.class);
        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(user.getLanguageCode()).thenReturn("en");

        Message message = mock(Message.class);
        when(message.getChatId()).thenReturn(100L + userId);

        when(cq.getId()).thenReturn(id);
        when(cq.getData()).thenReturn(data);
        when(cq.getFrom()).thenReturn(user);
        when(cq.getMessage()).thenReturn(message);

        return cq;
    }

    @Test
    @DisplayName("AnswerCallbackQuery is sent immediately BEFORE handler.handle executes")
    void testAnswerCallbackQuerySentBeforeHandlerExecution() throws Exception {
        CallbackQuery callbackQuery = createMockCallbackQuery("cb-123", "menu_action", 1L);
        when(testHandler.isHandle("menu_action")).thenReturn(true);

        AtomicBoolean answeredBeforeHandler = new AtomicBoolean(false);

        when(testHandler.handle(any(), any(), any(), any())).thenAnswer(invocation -> {
            // Check that telegramClient.execute(AnswerCallbackQuery) was ALREADY called at this point!
            try {
                verify(telegramClient, times(1)).execute(any(AnswerCallbackQuery.class));
                answeredBeforeHandler.set(true);
            } catch (Throwable t) {
                answeredBeforeHandler.set(false);
            }
            return HandleResult.success().build();
        });

        boolean handled = buttonService.handle(callbackQuery);

        assertThat(handled).isTrue();
        assertThat(answeredBeforeHandler.get())
                .as("AnswerCallbackQuery must have been executed before handler.handle started")
                .isTrue();

        ArgumentCaptor<AnswerCallbackQuery> captor = ArgumentCaptor.forClass(AnswerCallbackQuery.class);
        verify(telegramClient, times(1)).execute(captor.capture());
        assertThat(captor.getValue().getCallbackQueryId()).isEqualTo("cb-123");
    }

    @Test
    @DisplayName("AnswerCallbackQuery is sent even if handler takes a long time (simulating virtual thread blocking)")
    void testAnswerCallbackQueryWithSlowHandlerInVirtualThread() throws Exception {
        CallbackQuery callbackQuery = createMockCallbackQuery("cb-slow", "slow_action", 2L);
        when(testHandler.isHandle("slow_action")).thenReturn(true);

        CountDownLatch handlerStarted = new CountDownLatch(1);
        CountDownLatch handlerFinished = new CountDownLatch(1);

        when(testHandler.handle(any(), any(), any(), any())).thenAnswer(invocation -> {
            handlerStarted.countDown();
            // Simulate virtual thread blocking on I/O or external service
            Thread.sleep(150);
            handlerFinished.countDown();
            return HandleResult.success().build();
        });

        AtomicBoolean result = new AtomicBoolean(false);
        Thread vt = Thread.ofVirtual().name("test-vt").start(() -> {
            result.set(buttonService.handle(callbackQuery));
        });

        // Wait until handler starts
        boolean started = handlerStarted.await(2, TimeUnit.SECONDS);
        assertThat(started).isTrue();

        // While handler is still sleeping/blocked, verify AnswerCallbackQuery was ALREADY sent!
        verify(telegramClient, times(1)).execute(argThat((AnswerCallbackQuery a) ->
                "cb-slow".equals(a.getCallbackQueryId())
        ));

        // Wait for thread to finish
        vt.join(2000);
        assertThat(result.get()).isTrue();
    }

    @Test
    @DisplayName("AnswerCallbackQuery is sent even if handler throws an exception")
    void testAnswerCallbackQueryWhenHandlerThrowsException() throws Exception {
        CallbackQuery callbackQuery = createMockCallbackQuery("cb-err", "error_action", 3L);
        when(testHandler.isHandle("error_action")).thenReturn(true);

        when(testHandler.handle(any(), any(), any(), any()))
                .thenThrow(new TelegramApiException("Simulated API failure"));

        boolean handled = buttonService.handle(callbackQuery);

        assertThat(handled).isTrue(); // Handled as error result
        verify(telegramClient, times(1)).execute(argThat((AnswerCallbackQuery a) ->
                "cb-err".equals(a.getCallbackQueryId())
        ));
    }

    @Test
    @DisplayName("Unknown callback query without matching handler still answers callback to stop spinner")
    void testUnknownCallbackQueryAnswersCallbackToStopSpinner() throws Exception {
        CallbackQuery callbackQuery = createMockCallbackQuery("cb-unknown", "unknown_cmd", 4L);
        when(testHandler.isHandle("unknown_cmd")).thenReturn(false);

        boolean handled = buttonService.handle(callbackQuery);

        assertThat(handled).isFalse();
        // Verifies AnswerCallbackQuery was still sent to stop the spinner in client
        verify(telegramClient, times(1)).execute(argThat((AnswerCallbackQuery a) ->
                "cb-unknown".equals(a.getCallbackQueryId())
        ));
        verify(testHandler, never()).handle(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Multiple concurrent virtual threads execute button handling safely without race condition")
    void testConcurrentVirtualThreadsHandlingButtons() throws Exception {
        int threadsCount = 50;
        when(testHandler.isHandle(any())).thenReturn(true);
        when(testHandler.handle(any(), any(), any(), any())).thenReturn(HandleResult.success().build());

        AtomicInteger successCount = new AtomicInteger(0);
        try (ExecutorService virtualExecutor = Executors.newVirtualThreadPerTaskExecutor()) {
            CountDownLatch latch = new CountDownLatch(threadsCount);
            for (int i = 0; i < threadsCount; i++) {
                final int idx = i;
                virtualExecutor.submit(() -> {
                    try {
                        CallbackQuery cq = createMockCallbackQuery("cb-" + idx, "action", (long) idx);
                        boolean ok = buttonService.handle(cq);
                        if (ok) {
                            successCount.incrementAndGet();
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            boolean completed = latch.await(5, TimeUnit.SECONDS);
            assertThat(completed).isTrue();
            assertThat(successCount.get()).isEqualTo(threadsCount);
            verify(telegramClient, times(threadsCount)).execute(any(AnswerCallbackQuery.class));
        }
    }

    @Test
    @DisplayName("handle(null) safely returns false without errors")
    void testHandleNullSafelyReturnsFalse() {
        boolean handled = buttonService.handle(null);
        assertThat(handled).isFalse();
        verifyNoInteractions(telegramClient);
    }
}
