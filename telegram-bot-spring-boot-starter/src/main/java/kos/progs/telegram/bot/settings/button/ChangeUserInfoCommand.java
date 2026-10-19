package kos.progs.telegram.bot.settings.button;

import kos.progs.telegram.bot.button.model.EmptyArgs;
import kos.progs.telegram.bot.button.service.ButtonService;
import org.springframework.stereotype.Service;

@Service
public class ChangeUserInfoCommand {
    public String getEmailChangeCommand() {
        return "change-email";
    }

    public String getPhoneChangeCommand() {
        return "change-phone";
    }

    public String buildEmailChangeCommand() {
        return ButtonService.buildCommand(getEmailChangeCommand(), new EmptyArgs());
    }

    public String buildPhoneChangeCommand() {
        return ButtonService.buildCommand(getPhoneChangeCommand(), new EmptyArgs());
    }
}
