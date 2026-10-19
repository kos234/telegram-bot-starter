package kos.progs.telegram.bot.payment.tinkoff.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kos.progs.telegram.bot.payment.tinkoff.service.TinkoffService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payments/tinkoff")
@Slf4j
@RequiredArgsConstructor
public class TBusinessController {
    private final TinkoffService tinkoffService;

    @PostMapping("/tinkoff-webhook")
    public ResponseEntity<String> handleTinkoffNotification(@RequestBody Map<String, Object> payload) {
        if (log.isDebugEnabled()) {
            log.debug("Notification received from Tinkoff");
            try {
                log.debug(new ObjectMapper().writeValueAsString(payload));
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize webhook payload to JSON: {}",
                        payload.entrySet().stream().map(vl -> vl.getKey() + "=" + vl.getValue()).collect(Collectors.joining(";")));
            }
        }

        tinkoffService.handleNotification(payload);
        return ResponseEntity.ok("OK");
    }
}
