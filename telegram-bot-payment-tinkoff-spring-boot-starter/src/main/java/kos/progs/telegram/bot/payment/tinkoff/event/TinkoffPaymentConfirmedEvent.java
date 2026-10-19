package kos.progs.telegram.bot.payment.tinkoff.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TinkoffPaymentConfirmedEvent extends ApplicationEvent {
    private final long orderId;
    private final long userId;

    public TinkoffPaymentConfirmedEvent(Object source, long orderId, long userId) {
        super(source);
        this.orderId = orderId;
        this.userId = userId;
    }
}
