package kos.progs.telegram.bot.lists.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Page {
    @Builder.Default
    private int pageSize = 1;
    @Builder.Default
    private int page = 0;
    public static final Page ALL = Page.builder().page(0).pageSize(-1).build();
}
