package kos.progs.telegram.bot.settings.button;

import kos.progs.telegram.bot.button.model.PrimitiveArgs;
import kos.progs.telegram.bot.button.service.ButtonService;
import kos.progs.telegram.bot.common.model.Lang;
import org.springframework.stereotype.Service;

@Service
public class ChangeLangCommand {
    public String getBaseCommand() {
        return "settings-change-lang";
    }

    public String buildCommand(Lang lang) {
        return ButtonService.buildCommand(getBaseCommand(), new PrimitiveArgs<>(lang.getId()));
    }
}
