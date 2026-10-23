package kos.progs.telegram.bot.core.service;

import kos.progs.telegram.bot.button.service.ButtonService;
import kos.progs.telegram.bot.command.service.CommandService;
import kos.progs.telegram.bot.states.service.StateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import static kos.progs.telegram.bot.core.config.TelegramCoreConfig.TELEGRAM_BOT_EXECUTOR_NAME;

@RequiredArgsConstructor
@Slf4j
public class BaseBotUpdater {
    private final CommandService commandService;
    private final StateService stateService;
    private final ButtonService buttonService;

    @Async(TELEGRAM_BOT_EXECUTOR_NAME)
    public void update(Update update) {
        if (update == null) {
            return;
        }

        try {
            if (update.getCallbackQuery() != null && buttonService.handle(update.getCallbackQuery())) {
                return;
            }

            Message message = update.getMessage();
            if (message != null) {
                if (commandService.handle(message)) {
                    return;
                }

                if (stateService.handle(message)) {
                    return;
                }
            }
        } catch (Exception e) {
            log.error("Unhandled error processing update id {}: {}", update.getUpdateId(), e.getMessage(), e);
        }
    }
}
