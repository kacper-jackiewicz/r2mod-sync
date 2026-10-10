package dev.kacperjackiewicz.r2mod_sync.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GameServerControlService {

    private final String processName;

    public GameServerControlService(
            @Value("${game-server.process-name:r2modman-headless}") String processName) {
        this.processName = processName;
    }

    public Health getHealth() {
        return new Health(processName, ServerStatus.STOPPED);
    }

    public void start() {
        throw integrationUnavailable();
    }

    public void stop() {
        throw integrationUnavailable();
    }

    public void restart() {
        throw integrationUnavailable();
    }

    private ResponseStatusException integrationUnavailable() {
        return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "Game server control is unavailable until the server integration is configured.");
    }

    public record Health(String processName, ServerStatus status) {
    }

    public enum ServerStatus {
        NOT_CONFIGURED,
        RUNNING,
        STOPPED
    }
}
