package kos.progs.telegram.bot.payment.tinkoff.service;

import kos.progs.telegram.bot.admin.service.AdminService;
import kos.progs.telegram.bot.payment.tinkoff.config.TinkoffPaymentProperties;
import kos.progs.telegram.bot.payment.tinkoff.repository.TinkoffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TinkoffServiceTest {

    @Mock
    private TinkoffRepository tinkoffRepository;

    @Mock
    private AdminService adminService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private TinkoffPaymentProperties properties;
    private TinkoffService tinkoffService;

    @BeforeEach
    void setUp() {
        properties = new TinkoffPaymentProperties();
        properties.setPassword("test_pass");
        properties.setTerminalKey("test_terminal");

        tinkoffService = new TinkoffService(
                tinkoffRepository,
                adminService,
                eventPublisher,
                null,
                properties
        );
    }

    @Test
    void testGenerateToken() throws Exception {
        Map<String, Object> request = Map.of(
                "TerminalKey", "test_terminal",
                "Amount", 1000,
                "OrderId", "123"
        );

        String token = tinkoffService.generateToken(1L, 123L, request);
        assertNotNull(token);
        assertEquals(64, token.length()); // SHA-256 hex string length
    }
}
