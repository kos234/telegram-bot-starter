package kos.progs.telegram.bot.spi;

import kos.progs.telegram.bot.common.model.Lang;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

/**
 * SPI interface allowing modular providers to inject inline buttons into the admin panel (/admin).
 */
public interface AdminMenuItemProvider {
    /**
     * Determines order in which provider button rows appear. Lower numbers appear first.
     */
    int getOrder();

    /**
     * Returns an inline keyboard row to display in the admin menu.
     */
    InlineKeyboardRow getButtonRow(long adminId, Lang lang);
}
