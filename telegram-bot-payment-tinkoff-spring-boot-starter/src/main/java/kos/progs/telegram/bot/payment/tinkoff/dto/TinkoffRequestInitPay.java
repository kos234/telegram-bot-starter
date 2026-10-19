package kos.progs.telegram.bot.payment.tinkoff.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class TinkoffRequestInitPay {
    @JsonProperty("TerminalKey")
    private String terminalKey;

    @JsonProperty("Amount")
    private long amount;

    @JsonProperty("OrderId")
    private String orderId;

    @JsonProperty("CustomerKey")
    private String customerKey;

    @JsonProperty("Description")
    private String description;

    @JsonProperty("RedirectDueDate")
    private String redirectDueDate;

    @JsonProperty("NotificationURL")
    @JsonIgnore
    private String notificationURL;

    @JsonProperty("Receipt")
    private TinkoffRequestReceipt receipt;

    @JsonProperty("Token")
    private String token;
}
