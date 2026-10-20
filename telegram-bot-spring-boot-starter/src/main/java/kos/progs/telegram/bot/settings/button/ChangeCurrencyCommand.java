package kos.progs.telegram.bot.settings.button;

import kos.progs.telegram.bot.button.model.PrimitiveArgs;
import kos.progs.telegram.bot.button.service.ButtonService;
import kos.progs.telegram.bot.common.model.Currency;

public class ChangeCurrencyCommand {
    public String getBaseCommand() {
        return "settings-change-currency";
    }

    public String buildCommand(Currency currency) {
        return ButtonService.buildCommand(getBaseCommand(), new PrimitiveArgs<>(currency.getId()));
    }
}
