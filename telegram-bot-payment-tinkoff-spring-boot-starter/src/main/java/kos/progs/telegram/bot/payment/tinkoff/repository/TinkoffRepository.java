package kos.progs.telegram.bot.payment.tinkoff.repository;

import kos.progs.telegram.bot.payment.tinkoff.dto.TinkofURLDTO;

import java.time.ZonedDateTime;
import java.util.Optional;

public interface TinkoffRepository {
    void savePaymentId(long orderId, TinkofURLDTO urls, ZonedDateTime expTime);
    Optional<TinkofURLDTO> getActiveUrl(long orderId);
}
