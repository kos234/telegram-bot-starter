package kos.progs.telegram.bot.lists.button;

import kos.progs.telegram.bot.button.service.ButtonService;
import kos.progs.telegram.bot.lists.model.AbstractMoveArgs;
import kos.progs.telegram.bot.lists.model.AbstractMoveToArgs;

public abstract class AbstractMoveCommand {
    public abstract String getPrefix();

    public String getNextCommand() {
        return getPrefix() + "next";
    }

    public String getPrevCommand() {
        return getPrefix() + "prev";
    }

    public String getByCommand() {
        return getPrefix() + "byi";
    }

    public String buildNextCommand(AbstractMoveArgs args) {
        return ButtonService.buildCommand(getNextCommand(), args);
    }

    public String buildPrevCommand(AbstractMoveArgs args) {
        return ButtonService.buildCommand(getPrevCommand(), args);
    }

    public String buildByCommand(AbstractMoveToArgs args) {
        return ButtonService.buildCommand(getByCommand(), args);
    }
}
