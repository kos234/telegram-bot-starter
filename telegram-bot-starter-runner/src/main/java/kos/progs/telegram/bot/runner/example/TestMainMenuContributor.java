package kos.progs.telegram.bot.runner.example;

import kos.progs.telegram.bot.common.model.Lang;
import kos.progs.telegram.bot.main_menu.MainMenuContributor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

@Component
public class TestMainMenuContributor implements MainMenuContributor {

    @Override
    public int getOrder() {
        return 10;
    }

    @Override
    public List<KeyboardRow> getRows(long userId, Lang lang) {
        KeyboardRow row = new KeyboardRow();
        row.add(new KeyboardButton("🧪 /ping"));
        return List.of(row);
    }
}
