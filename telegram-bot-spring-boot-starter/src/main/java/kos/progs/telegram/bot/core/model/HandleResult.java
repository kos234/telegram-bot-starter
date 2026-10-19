package kos.progs.telegram.bot.core.model;

import kos.progs.telegram.bot.states.model.HistoryAction;
import kos.progs.telegram.bot.states.model.StateFrame;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.With;

@Getter
@AllArgsConstructor
@With
public class HandleResult {
    private HandleStatus status;
    private String errorMessage;
    private HistoryAction actionHistory;
    private int rollbackCount;
    private String history;
    private boolean updateState;
    private StateFrame newState;

    public static final HandleResult UNHANDLED = unhandled();

    public boolean isHandle() {
        return status == HandleStatus.HANDLED_SUCCESS || status == HandleStatus.HANDLED_ERROR;
    }

    public boolean isSuccess() {
        return status == HandleStatus.HANDLED_SUCCESS;
    }

    public boolean isError() {
        return status == HandleStatus.HANDLED_ERROR;
    }

    public boolean isUnhandled() {
        return status == HandleStatus.UNHANDLED;
    }

    public static HandleResult unhandled() {
        return new HandleResult(HandleStatus.UNHANDLED, null, HistoryAction.IGNORE, 1, null, false, null);
    }

    public static HandleResult error(String message) {
        return new HandleResult(HandleStatus.HANDLED_ERROR, message, HistoryAction.IGNORE, 1, null, false, null);
    }

    public static HandleResultBuilder success() {
        return new HandleResultBuilder().success();
    }

    public String getHistory(String orNull) {
        return history == null ? orNull : history;
    }

    public static class HandleResultBuilder {
        private HandleStatus status = HandleStatus.UNHANDLED;
        private String errorMessage = null;
        private HistoryAction historyAction = HistoryAction.IGNORE;
        private int rollbackCount = 1;
        private String history = null;
        private boolean updateState = false;
        private StateFrame newState = null;

        public HandleResult build() {
            return new HandleResult(status, errorMessage, historyAction, rollbackCount, history, updateState, newState);
        }

        public HandleResult failure() {
            this.status = HandleStatus.UNHANDLED;
            this.errorMessage = null;
            this.historyAction = HistoryAction.IGNORE;
            this.history = null;
            this.updateState = false;
            this.newState = null;
            return build();
        }

        public HandleResultBuilder success() {
            this.status = HandleStatus.HANDLED_SUCCESS;
            this.historyAction = HistoryAction.ADD;
            return this;
        }

        public HandleResultBuilder error(String message) {
            this.status = HandleStatus.HANDLED_ERROR;
            this.errorMessage = message;
            return this;
        }

        public HandleResultBuilder history(HistoryAction historyAction) {
            this.historyAction = historyAction;
            return this;
        }

        public HandleResultBuilder historyRollback(int count) {
            this.historyAction = HistoryAction.ROLLBACK;
            this.rollbackCount = count;
            return this;
        }

        public HandleResultBuilder history(HistoryAction historyAction, String history) {
            this.historyAction = historyAction;
            this.history = history;
            return this;
        }

        public HandleResultBuilder state(StateFrame newState) {
            this.updateState = true;
            this.newState = newState;
            return this;
        }
    }
}
