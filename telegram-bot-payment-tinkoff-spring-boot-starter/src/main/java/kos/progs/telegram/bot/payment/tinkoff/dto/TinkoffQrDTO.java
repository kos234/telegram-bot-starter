package kos.progs.telegram.bot.payment.tinkoff.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TinkoffQrDTO {
    @JsonProperty("Success")
    public Boolean success;

    @JsonProperty("ErrorCode")
    public String errorCode;

    @JsonProperty("Data")
    public String data;

    @JsonProperty("Message")
    public String message;

    @JsonProperty("Details")
    public String details;
}
