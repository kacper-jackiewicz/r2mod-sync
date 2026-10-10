package dev.kj.r2mod_sync.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionHandler {

    @org.springframework.web.bind.annotation.ExceptionHandler(OperationInProgressException.class)
    public ResponseEntity<Map<String, String>> operationInProgress() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("messageKey", "server.operationInProgress"));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(ServerNotRunningException.class)
    public ResponseEntity<Map<String, String>> serverNotRunning() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("messageKey", "server.notRunning"));
    }
}
