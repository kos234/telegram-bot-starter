package kos.progs.telegram.bot.core.model;

/**
 * Lifecycle status of handling a Telegram update.
 */
public enum HandleStatus {
    /**
     * Handler skipped or didn't match the message; router continues trying remaining handlers.
     */
    UNHANDLED,

    /**
     * Message was successfully handled; processing chain is stopped.
     */
    HANDLED_SUCCESS,

    /**
     * An error occurred while handling the message; processing chain is stopped.
     */
    HANDLED_ERROR
}
