package kos.progs.telegram.bot.payment.tinkoff.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kos.progs.telegram.bot.admin.service.AdminService;
import kos.progs.telegram.bot.payment.tinkoff.config.TinkoffPaymentProperties;
import kos.progs.telegram.bot.payment.tinkoff.dto.*;
import kos.progs.telegram.bot.payment.tinkoff.event.TinkoffPaymentConfirmedEvent;
import kos.progs.telegram.bot.payment.tinkoff.repository.TinkoffRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Slf4j
public class TinkoffService {
    private static final String BASE_URL = "https://securepay.tinkoff.ru/v2/";
    private static final String TEST_URL = "https://rest-api-test.tinkoff.ru/v2/";
    private static final int LIFETIME_MINUTES = 30;

    private final ObjectMapper mapper;
    private final HttpClient httpClient;
    private final TinkoffRepository tinkoffRepository;
    private final AdminService adminService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final TinkoffPaymentOrderService orderService;
    private final TinkoffPaymentProperties properties;

    @Value("${SERVER_URL:}")
    private String envServerUrl;
    @Value("${TINKOFF_TERMINAL_KEY:}")
    private String envTerminalKey;
    @Value("${TINKOFF_PASSWORD:}")
    private String envPassword;
    @Value("${DEBUG_TINKOFF_TERMINAL_KEY:}")
    private String envDebugTerminalKey;
    @Value("${DEBUG_TINKOFF_PASSWORD:}")
    private String envDebugPassword;
    @Value("${DEBUG_TINKOFF_MODE:0}")
    private int envDebugTinkoffMode;
    @Value("${DEBUG_MODE:0}")
    private int envDebugMode;

    public TinkoffService(TinkoffRepository tinkoffRepository,
                          AdminService adminService,
                          ApplicationEventPublisher applicationEventPublisher,
                          @Autowired(required = false) TinkoffPaymentOrderService orderService,
                          TinkoffPaymentProperties properties) {
        this.tinkoffRepository = tinkoffRepository;
        this.adminService = adminService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.orderService = orderService;
        this.properties = properties;
        this.mapper = new ObjectMapper();
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    public int getId() {
        return 0;
    }

    public Optional<TinkofURLDTO> getActiveUrl(long orderId) {
        return tinkoffRepository.getActiveUrl(orderId);
    }

    public String generateToken(long userId, long orderId, Object requestObject) throws Exception {
        Map<String, Object> params = mapper.convertValue(
                requestObject,
                new TypeReference<Map<String, Object>>() {}
        );

        Map<String, Object> copyParams = new TreeMap<>(params);
        copyParams.put("Password", getPassword(userId, orderId));

        copyParams.remove("Token");
        copyParams.remove("Receipt");
        copyParams.remove("DATA");
        copyParams.remove("Data");

        StringBuilder sb = new StringBuilder();
        for (Object value : copyParams.values()) {
            if (value != null) {
                sb.append(value.toString());
            }
        }

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(sb.toString().getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public TinkofURLDTO initPayment(long orderId,
                                    long userId,
                                    String email,
                                    String phone,
                                    BigDecimal amountKopecks,
                                    String description,
                                    List<TinkoffRequestReceiptItem> items) throws RuntimeException {
        try {
            ZonedDateTime dueDate = ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS).plusMinutes(LIFETIME_MINUTES);
            String serverUrl = getServerUrl();

            BigDecimal finalAmount = amountKopecks;
            if (adminService.isUserAdmin(userId) && getDebugMode() == 1 && getDebugTinkoffMode() == 2) {
                log.info("DEBUG mode enabled for admin {}, using debug amount 100 kopecks for order {}", userId, orderId);
                finalAmount = BigDecimal.valueOf(100);
            }

            TinkoffRequestInitPay initPayRequestData = TinkoffRequestInitPay.builder()
                    .terminalKey(getTerminalKey(userId, orderId))
                    .amount(finalAmount.longValue())
                    .orderId(String.valueOf(orderId))
                    .customerKey(String.valueOf(userId))
                    .description(description)
                    .redirectDueDate(dueDate.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                    .notificationURL(serverUrl != null && !serverUrl.isEmpty() ? serverUrl + "/api/payments/tinkoff/tinkoff-webhook" : null)
                    .receipt(TinkoffRequestReceipt.builder()
                            .email(email)
                            .phone(phone)
                            .taxation("osn")
                            .items(items != null ? items : Collections.emptyList())
                            .build()
                    )
                    .build();
            initPayRequestData.setToken(generateToken(userId, orderId, initPayRequestData));

            HttpRequest requestInit = HttpRequest.newBuilder()
                    .uri(URI.create(getURL(userId, orderId) + "Init"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(initPayRequestData)))
                    .build();

            HttpResponse<String> response = httpClient.send(requestInit, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Tinkoff Init HTTP error " + response.statusCode() + ": " + response.body());
            }

            TinkoffInitDTO responseDto = mapper.readValue(response.body(), new TypeReference<TinkoffInitDTO>() {});
            if (!Boolean.TRUE.equals(responseDto.success)) {
                throw new RuntimeException("Tinkoff Init error: " + responseDto.message + ", details: " + responseDto.details);
            }

            String qrUrl = null;
            try {
                TinkoffRequestQR requestQRData = TinkoffRequestQR.builder()
                        .terminalKey(getTerminalKey(userId, orderId))
                        .paymentId(responseDto.paymentId)
                        .dataType("PAYLOAD")
                        .build();
                requestQRData.setToken(generateToken(userId, orderId, requestQRData));

                HttpRequest requestQr = HttpRequest.newBuilder()
                        .uri(URI.create(getURL(userId, orderId) + "GetQr"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(requestQRData)))
                        .build();

                HttpResponse<String> responseQr = httpClient.send(requestQr, HttpResponse.BodyHandlers.ofString());
                if (responseQr.statusCode() == 200) {
                    TinkoffQrDTO qrDto = mapper.readValue(responseQr.body(), new TypeReference<TinkoffQrDTO>() {});
                    if (Boolean.TRUE.equals(qrDto.success)) {
                        qrUrl = qrDto.data;
                    } else {
                        log.warn("Tinkoff GetQr unsuccessful: {} ({})", qrDto.message, qrDto.details);
                    }
                }
            } catch (Exception e) {
                log.warn("SBP (QR) payment unavailable: {}", e.getMessage());
            }

            var ans = new TinkofURLDTO(responseDto.paymentId, responseDto.paymentURL, qrUrl);
            tinkoffRepository.savePaymentId(orderId, ans, dueDate);
            return ans;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Tinkoff payment for order " + orderId, e);
        }
    }

    public boolean handleNotification(Map<String, Object> payload) {
        try {
            long orderId = Long.parseLong((String) payload.get("OrderId"));
            long userId = Long.parseLong((String) payload.get("CustomerKey"));

            String receivedToken = (String) payload.get("Token");
            String calculatedToken = generateToken(userId, orderId, payload);

            if (!calculatedToken.equals(receivedToken)) {
                log.warn("Tinkoff webhook token mismatch for orderId {}", orderId);
                return false;
            }

            String status = (String) payload.get("Status");
            if ("CONFIRMED".equalsIgnoreCase(status)) {
                if (orderService != null) {
                    orderService.handlePayedOrder(orderId, userId);
                }
                applicationEventPublisher.publishEvent(new TinkoffPaymentConfirmedEvent(this, orderId, userId));
                return true;
            }

            return false;
        } catch (Exception e) {
            log.error("Error processing Tinkoff notification", e);
            return false;
        }
    }

    private String getServerUrl() {
        if (properties.getServerUrl() != null && !properties.getServerUrl().isBlank()) {
            return properties.getServerUrl();
        }
        return envServerUrl;
    }

    private int getDebugMode() {
        return properties.getDebugMode() != 0 ? properties.getDebugMode() : envDebugMode;
    }

    private int getDebugTinkoffMode() {
        return properties.getDebugTinkoffMode() != 0 ? properties.getDebugTinkoffMode() : envDebugTinkoffMode;
    }

    private String getTerminalKey(long userId, long orderId) {
        if (adminService.isUserAdmin(userId) && getDebugMode() == 1 && getDebugTinkoffMode() == 1) {
            log.info("DEBUG mode enabled for admin {}, using debug terminal key for order {}", userId, orderId);
            return properties.getDebugTerminalKey() != null ? properties.getDebugTerminalKey() : envDebugTerminalKey;
        }
        return properties.getTerminalKey() != null ? properties.getTerminalKey() : envTerminalKey;
    }

    private String getPassword(long userId, long orderId) {
        if (adminService.isUserAdmin(userId) && getDebugMode() == 1 && getDebugTinkoffMode() == 1) {
            log.info("DEBUG mode enabled for admin {}, using debug password for order {}", userId, orderId);
            return properties.getDebugPassword() != null ? properties.getDebugPassword() : envDebugPassword;
        }
        return properties.getPassword() != null ? properties.getPassword() : envPassword;
    }

    private String getURL(long userId, long orderId) {
        if (adminService.isUserAdmin(userId) && getDebugMode() == 1 && getDebugTinkoffMode() == 1) {
            return TEST_URL;
        }
        return BASE_URL;
    }
}
