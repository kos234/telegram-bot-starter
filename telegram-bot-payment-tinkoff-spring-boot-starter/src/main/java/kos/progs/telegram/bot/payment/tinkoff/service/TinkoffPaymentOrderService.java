package kos.progs.telegram.bot.payment.tinkoff.service;

/**
 * Callback interface for receiving payment confirmations in the host application.
 * Typically implemented by OrderService in shop_bot.
 */
public interface TinkoffPaymentOrderService {
    void handlePayedOrder(long orderId, long userId);
}
