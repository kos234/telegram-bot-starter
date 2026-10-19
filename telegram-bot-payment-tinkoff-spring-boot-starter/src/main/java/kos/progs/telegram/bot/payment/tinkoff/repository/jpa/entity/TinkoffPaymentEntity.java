package kos.progs.telegram.bot.payment.tinkoff.repository.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

@Entity
@Table(name = "tinkoff_payments")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TinkoffPaymentEntity {
    @Id
    private Long orderId;

    @Column(nullable = false)
    private String paymentId;

    @Column(columnDefinition = "TEXT")
    private String tUrl;

    @Column(columnDefinition = "TEXT")
    private String qrUrl;

    private ZonedDateTime expTime;
}
