package kos.progs.telegram.bot.states.repository;

import kos.progs.telegram.bot.states.model.StateFrame;

import java.util.Optional;

public interface StateRepository {
    Optional<StateFrame> getChatState(long chatId);
    void setChatState(long chatId, StateFrame frame);
}
