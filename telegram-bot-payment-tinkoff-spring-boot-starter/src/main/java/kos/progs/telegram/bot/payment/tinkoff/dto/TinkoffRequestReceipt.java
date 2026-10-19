package kos.progs.telegram.bot.payment.tinkoff.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class TinkoffRequestReceipt {
    @JsonProperty("Items")
    private List<TinkoffRequestReceiptItem> items;

    @JsonProperty("Email")
    private String email;

    @JsonProperty("Phone")
    private String phone;

    @JsonProperty("Taxation")
    private String taxation;
}
