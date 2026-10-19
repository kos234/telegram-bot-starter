package kos.progs.telegram.bot.states.repository.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bot_chat_state")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StateEntity {
    @Id
    private Long chatId;

    @Column(columnDefinition = "TEXT")
    private String jsonState;
}
