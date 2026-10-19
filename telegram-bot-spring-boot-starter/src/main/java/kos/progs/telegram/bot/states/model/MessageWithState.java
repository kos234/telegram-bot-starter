package kos.progs.telegram.bot.states.model;

import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Getter
@Setter
public class MessageWithState extends Message {
    private String jsonState;
}
