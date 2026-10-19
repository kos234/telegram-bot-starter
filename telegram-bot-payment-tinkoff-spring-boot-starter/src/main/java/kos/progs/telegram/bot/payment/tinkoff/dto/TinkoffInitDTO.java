package kos.progs.telegram.bot.payment.tinkoff.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TinkoffInitDTO {
    @JsonProperty("Success")
    public Boolean success;

    @JsonProperty("ErrorCode")
    public String errorCode;

    @JsonProperty("Status")
    public String status;

    @JsonProperty("PaymentId")
    public String paymentId;

    @JsonProperty("PaymentURL")
    public String paymentURL;

    @JsonProperty("Message")
    public String message;

    @JsonProperty("Details")
    public String details;
}
