package kos.progs.telegram.bot.lists.model;

import java.util.List;

public record ListAns<T>(List<T> result, int globalCount) {
}
