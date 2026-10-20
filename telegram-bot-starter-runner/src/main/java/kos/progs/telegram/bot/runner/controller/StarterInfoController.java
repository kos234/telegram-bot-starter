package kos.progs.telegram.bot.runner.controller;

import kos.progs.telegram.bot.common.model.Currency;
import kos.progs.telegram.bot.common.model.Lang;
import kos.progs.telegram.bot.core.config.TelegramBotProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class StarterInfoController {

    private final TelegramBotProperties properties;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "app", "telegram-bot-starter-runner"
        ));
    }

    @GetMapping("/starter/info")
    public ResponseEntity<Map<String, Object>> starterInfo() {
        return ResponseEntity.ok(Map.of(
                "stateStorage", properties.getStateStorage(),
                "userStorage", properties.getUserStorage(),
                "adminStorage", properties.getAdminStorage(),
                "languages", Lang.all().stream().map(l -> Map.of("id", l.getId(), "code", l.getCode(), "title", l.getTitle())).toList(),
                "currencies", Currency.all().stream().map(c -> Map.of("id", c.getId(), "code", c.getCode(), "title", c.getTitle())).toList(),
                "languageEnabled", properties.getLanguage().isEnabled(),
                "currencyEnabled", properties.getCurrency().isEnabled(),
                "emailEnabled", properties.getEmail().isEnabled(),
                "phoneEnabled", properties.getPhone().isEnabled()
        ));
    }
}
