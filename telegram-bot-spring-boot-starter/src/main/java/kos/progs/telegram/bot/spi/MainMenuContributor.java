package kos.progs.telegram.bot.spi;

import kos.progs.telegram.bot.common.model.Lang;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

/**
 * SPI interface allowing modular contributors to inject rows into the main (/start) keyboard.
 */
public interface MainMenuContributor {
    /**
     * Determines the order in which rows from contributors are appended. Lower numbers appear first.
     */
    int getOrder();

    /**
     * Returns keyboard rows to display for the specified user and language.
     */
    List<KeyboardRow> getRows(long userId, Lang lang);
}
