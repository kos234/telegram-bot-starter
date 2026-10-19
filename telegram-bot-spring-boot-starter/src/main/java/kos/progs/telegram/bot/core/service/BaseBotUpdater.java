package kos.progs.telegram.bot.core.service;

import kos.progs.telegram.bot.button.service.ButtonService;
import kos.progs.telegram.bot.command.service.CommandService;
import kos.progs.telegram.bot.states.service.StateService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class BaseBotUpdater {
    private final CommandService commandService;
    private final StateService stateService;
    private final ButtonService buttonService;

    @Async
    public void update(Update update) {
        if (update == null) {
            return;
        }

        if (commandService.handle(update.getMessage())) {
            return;
        }

        if (stateService.handle(update.getMessage())) {
            return;
        }

        if (buttonService.handle(update.getCallbackQuery())) {
            return;
        }
    }
}
