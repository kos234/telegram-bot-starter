package kos.progs.telegram.bot.admin.repository.jpa.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bot_admins")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdminUserEntity {
    @Id
    private Long telegramUserId;
}
