package dev.kj.r2mod_sync.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExceptionHandlerTest {

    private final ExceptionHandler handler = new ExceptionHandler();

    @Test
    void operationInProgressReturnsConflictAndMessageKey() {
        var response = handler.operationInProgress();

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("server.operationInProgress", response.getBody().get("messageKey"));
    }

    @Test
    void serverNotRunningReturnsConflictAndMessageKey() {
        var response = handler.serverNotRunning();

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("server.notRunning", response.getBody().get("messageKey"));
    }
}
