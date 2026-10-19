package kos.progs.telegram.bot.lists.states;

import kos.progs.telegram.bot.states.model.StateFrame;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GoToPageState implements StateFrame {
    private String prefix;
    private int messageId;
    private byte[] args;
}
