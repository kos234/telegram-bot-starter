package kos.progs.telegram.bot.runner.example;

import kos.progs.telegram.bot.command.handler.CommandHandler;
import kos.progs.telegram.bot.core.model.HandleResult;
import kos.progs.telegram.bot.user.model.UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class TestPingCommandHandler implements CommandHandler {

    private final TelegramClient telegramClient;

    @Override
    public boolean isHandle(String command, String lastHistory) {
        return "/ping".equalsIgnoreCase(command) || "ping".equalsIgnoreCase(command);
    }

    @Override
    public HandleResult handle(Message message, UserInfo userInfo, String command) throws TelegramApiException {
        log.info("Received /ping command from user {}", userInfo.userId());
        telegramClient.execute(SendMessage.builder()
                .chatId(message.getChatId())
                .text("🏓 Pong! Starter is running smoothly.")
                .build());
        return HandleResult.success().build();
    }
}
