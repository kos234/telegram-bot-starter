package kos.progs.telegram.bot.command.service;

import kos.progs.telegram.bot.command.handler.CommandHandler;
import kos.progs.telegram.bot.core.model.HandleResult;
import kos.progs.telegram.bot.states.handler.StateHandler;
import kos.progs.telegram.bot.states.model.StateFrame;
import kos.progs.telegram.bot.states.repository.StateRepository;
import kos.progs.telegram.bot.states.service.NavigationService;
import kos.progs.telegram.bot.states.service.StateService;
import kos.progs.telegram.bot.user.model.UserInfo;
import kos.progs.telegram.bot.user.service.UserInfoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CommandServiceAndStateServiceVirtualThreadTest {

    @Test
    @DisplayName("CommandService handles concurrent commands in virtual threads safely")
    void testCommandServiceConcurrentVirtualThreads() throws Exception {
        CommandHandler handler = mock(CommandHandler.class);
        when(handler.isHandle(any(), any())).thenReturn(true);
        when(handler.handle(any(), any(), any())).thenReturn(HandleResult.success().build());

        StateService stateService = mock(StateService.class);
        NavigationService navigationService = mock(NavigationService.class);
        UserInfoService userInfoService = mock(UserInfoService.class);

        CommandService commandService = new CommandService(
                List.of(handler),
                stateService,
                navigationService,
                userInfoService
        );

        int count = 50;
        AtomicInteger handledCount = new AtomicInteger(0);

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            CountDownLatch latch = new CountDownLatch(count);
            for (int i = 0; i < count; i++) {
                final int idx = i;
                executor.submit(() -> {
                    try {
                        Message msg = mock(Message.class);
                        User user = mock(User.class);
                        when(user.getId()).thenReturn((long) idx);
                        when(user.getLanguageCode()).thenReturn("ru");
                        when(msg.getFrom()).thenReturn(user);
                        when(msg.getChatId()).thenReturn((long) (idx + 100));
                        when(msg.getText()).thenReturn("/start");

                        if (commandService.handle(msg)) {
                            handledCount.incrementAndGet();
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            boolean finished = latch.await(5, TimeUnit.SECONDS);
            assertThat(finished).isTrue();
            assertThat(handledCount.get()).isEqualTo(count);
        }
    }

    @Test
    @DisplayName("CommandService safely ignores null message, null text, and null from")
    void testCommandServiceEdgeCases() {
        CommandService commandService = new CommandService(
                List.of(),
                mock(StateService.class),
                mock(NavigationService.class),
                mock(UserInfoService.class)
        );

        assertThat(commandService.handle(null)).isFalse();

        Message msgNoText = mock(Message.class);
        when(msgNoText.getText()).thenReturn(null);
        assertThat(commandService.handle(msgNoText)).isFalse();

        Message msgNoFrom = mock(Message.class);
        when(msgNoFrom.getText()).thenReturn("/cmd");
        when(msgNoFrom.getFrom()).thenReturn(null);
        assertThat(commandService.handle(msgNoFrom)).isFalse();
    }

    public static class DummyFrame implements StateFrame {
        public String data = "test";
    }

    @Test
    @DisplayName("StateService handles concurrent state messages in virtual threads safely")
    void testStateServiceConcurrentVirtualThreads() throws Exception {
        StateRepository stateRepository = mock(StateRepository.class);
        UserInfoService userInfoService = mock(UserInfoService.class);
        NavigationService navigationService = mock(NavigationService.class);
        StateHandler stateHandler = mock(StateHandler.class);

        DummyFrame dummyFrame = new DummyFrame();
        when(stateRepository.getChatState(anyLong())).thenReturn(Optional.of(dummyFrame));
        when(stateHandler.isHandle(any())).thenReturn(true);
        when(stateHandler.handle(any(), any(), any())).thenReturn(HandleResult.success().build());

        StateService stateService = new StateService(
                stateRepository,
                userInfoService,
                navigationService,
                List.of(stateHandler)
        );

        int count = 50;
        AtomicInteger handledCount = new AtomicInteger(0);

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            CountDownLatch latch = new CountDownLatch(count);
            for (int i = 0; i < count; i++) {
                final int idx = i;
                executor.submit(() -> {
                    try {
                        Message msg = mock(Message.class);
                        User user = mock(User.class);
                        when(user.getId()).thenReturn((long) idx);
                        when(user.getLanguageCode()).thenReturn("ru");
                        when(msg.getFrom()).thenReturn(user);
                        when(msg.getChatId()).thenReturn((long) (idx + 100));

                        if (stateService.handle(msg)) {
                            handledCount.incrementAndGet();
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            boolean finished = latch.await(5, TimeUnit.SECONDS);
            assertThat(finished).isTrue();
            assertThat(handledCount.get()).isEqualTo(count);
        }
    }

    @Test
    @DisplayName("StateService safely ignores null message and null from")
    void testStateServiceEdgeCases() {
        StateService stateService = new StateService(
                mock(StateRepository.class),
                mock(UserInfoService.class),
                mock(NavigationService.class),
                List.of()
        );

        assertThat(stateService.handle(null)).isFalse();

        Message msgNoFrom = mock(Message.class);
        when(msgNoFrom.getFrom()).thenReturn(null);
        assertThat(stateService.handle(msgNoFrom)).isFalse();
    }
}
