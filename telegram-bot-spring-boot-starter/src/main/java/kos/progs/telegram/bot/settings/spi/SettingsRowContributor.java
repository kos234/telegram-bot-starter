package kos.progs.telegram.bot.settings.spi;

import kos.progs.telegram.bot.user.model.UserInfo;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.List;

/**
 * SPI interface allowing bot implementations to add extra setting rows (e.g. delivery address)
 * and info lines to the settings menu.
 */
public interface SettingsRowContributor {
    int getOrder();
    List<InlineKeyboardRow> getRows(UserInfo userInfo);
    default String getExtraInfo(UserInfo userInfo) {
        return "";
    }
}
