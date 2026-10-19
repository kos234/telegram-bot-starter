package kos.progs.telegram.bot.payment.tinkoff.dto;

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
public class TinkoffRequestQR {
    @JsonProperty("TerminalKey")
    private String terminalKey;

    @JsonProperty("PaymentId")
    private String paymentId;

    @JsonProperty("DataType")
    private String dataType;

    @JsonProperty("Token")
    private String token;
}
