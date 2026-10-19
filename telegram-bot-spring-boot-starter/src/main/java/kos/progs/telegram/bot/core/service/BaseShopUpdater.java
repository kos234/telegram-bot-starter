package kos.progs.telegram.bot.core.service;

import kos.progs.telegram.bot.button.service.ButtonService;
import kos.progs.telegram.bot.command.service.CommandService;
import kos.progs.telegram.bot.states.service.StateService;

@Deprecated
public class BaseShopUpdater extends BaseBotUpdater {
    public BaseShopUpdater(CommandService commandService, StateService stateService, ButtonService buttonService) {
        super(commandService, stateService, buttonService);
    }
}
