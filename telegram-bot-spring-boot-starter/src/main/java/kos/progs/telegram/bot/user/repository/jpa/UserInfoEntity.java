package kos.progs.telegram.bot.user.repository.jpa;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kos.progs.telegram.bot.common.model.Currency;
import kos.progs.telegram.bot.common.model.Lang;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bot_user_info")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoEntity {
    @Id
    private Long userId;

    @Enumerated(EnumType.STRING)
    private Lang userLang;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    private String email;
    private String phone;
}
