package kos.progs.telegram.bot.admin.button;

import kos.progs.telegram.bot.button.service.ButtonService;
import org.springframework.stereotype.Service;

@Service
public class AdminsCommand {
    public String getViewListCommand() {
        return "admins-list";
    }

    public String buildViewListCommand() {
        return ButtonService.buildCommand(getViewListCommand());
    }

    public String getAddAdminCommand() {
        return "admin-list-add";
    }

    public String buildAddAdminCommand() {
        return ButtonService.buildCommand(getAddAdminCommand());
    }

    public String getRemoveAdminCommand() {
        return "admin-list-remove";
    }

    public String buildRemoveAdminCommand() {
        return ButtonService.buildCommand(getRemoveAdminCommand());
    }
}
