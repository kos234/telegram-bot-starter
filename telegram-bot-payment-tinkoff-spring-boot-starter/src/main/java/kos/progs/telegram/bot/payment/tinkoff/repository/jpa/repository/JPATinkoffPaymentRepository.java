package kos.progs.telegram.bot.payment.tinkoff.repository.jpa.repository;

import kos.progs.telegram.bot.payment.tinkoff.repository.jpa.entity.TinkoffPaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JPATinkoffPaymentRepository extends JpaRepository<TinkoffPaymentEntity, Long> {
}
