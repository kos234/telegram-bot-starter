package kos.progs.telegram.bot.settings.model;

import kos.progs.telegram.bot.states.model.StateFrame;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@Builder
public class UserInfoSetState implements StateFrame {
    public static final int CHANGE_EMAIL = 0;
    public static final int CHANGE_PHONE = 1;

    private int change;

    public UserInfoSetState(int change) {
        setChange(change);
    }

    public void setChange(int change) {
        if (change != CHANGE_EMAIL && change != CHANGE_PHONE) {
            throw new IllegalArgumentException("Unsupported change state = " + change);
        }
        this.change = change;
    }
}
