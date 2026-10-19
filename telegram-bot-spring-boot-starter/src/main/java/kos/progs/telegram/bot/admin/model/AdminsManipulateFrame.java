package kos.progs.telegram.bot.admin.model;

import kos.progs.telegram.bot.states.model.StateFrame;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AdminsManipulateFrame implements StateFrame {
    private boolean isAdd;
}
