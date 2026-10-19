package kos.progs.telegram.bot.payment.tinkoff.repository.jpa.adapters;

import kos.progs.telegram.bot.payment.tinkoff.dto.TinkofURLDTO;
import kos.progs.telegram.bot.payment.tinkoff.repository.TinkoffRepository;
import kos.progs.telegram.bot.payment.tinkoff.repository.jpa.entity.TinkoffPaymentEntity;
import kos.progs.telegram.bot.payment.tinkoff.repository.jpa.repository.JPATinkoffPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Optional;

@RequiredArgsConstructor
public class JPATinkoffPaymentRepositoryAdapter implements TinkoffRepository {
    private final JPATinkoffPaymentRepository jpaRepository;

    @Override
    @Transactional
    public void savePaymentId(long orderId, TinkofURLDTO urls, ZonedDateTime expTime) {
        if (urls != null) {
            jpaRepository.save(new TinkoffPaymentEntity(
                    orderId,
                    urls.paymentId(),
                    urls.tUrl(),
                    urls.qrUrl(),
                    expTime
            ));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TinkofURLDTO> getActiveUrl(long orderId) {
        return jpaRepository.findById(orderId).flatMap(entity -> {
            if (entity.getExpTime() != null && ZonedDateTime.now().isAfter(entity.getExpTime())) {
                return Optional.empty();
            }
            return Optional.of(new TinkofURLDTO(entity.getPaymentId(), entity.getTUrl(), entity.getQrUrl()));
        });
    }
}
