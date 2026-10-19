package kos.progs.telegram.bot.core.model;

import kos.progs.telegram.bot.states.model.HistoryAction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HandleResultTest {

    @Test
    void testUnhandled() {
        HandleResult result = HandleResult.unhandled();
        assertEquals(HandleStatus.UNHANDLED, result.getStatus());
        assertFalse(result.isHandle());
        assertTrue(result.isUnhandled());
        assertFalse(result.isSuccess());
        assertFalse(result.isError());
    }

    @Test
    void testSuccess() {
        HandleResult result = HandleResult.success()
                .history(HistoryAction.ADD, "/test")
                .build();

        assertEquals(HandleStatus.HANDLED_SUCCESS, result.getStatus());
        assertTrue(result.isHandle());
        assertTrue(result.isSuccess());
        assertFalse(result.isUnhandled());
        assertFalse(result.isError());
        assertEquals("/test", result.getHistory());
        assertEquals(HistoryAction.ADD, result.getActionHistory());
    }

    @Test
    void testError() {
        HandleResult result = HandleResult.error("Something went wrong");
        assertEquals(HandleStatus.HANDLED_ERROR, result.getStatus());
        assertTrue(result.isHandle());
        assertTrue(result.isError());
        assertFalse(result.isSuccess());
        assertFalse(result.isUnhandled());
        assertEquals("Something went wrong", result.getErrorMessage());
    }
}
