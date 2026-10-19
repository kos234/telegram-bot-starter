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
public class TinkoffRequestReceiptItem {
    @JsonProperty("Name")
    private String name;

    @JsonProperty("Price")
    private long price;

    @JsonProperty("Quantity")
    private int quantity;

    @JsonProperty("Amount")
    private long amount;

    @JsonProperty("Tax")
    private String tax;
}
