package dev.kacperjackiewicz.r2mod_sync.web;

import dev.kacperjackiewicz.r2mod_sync.config.GameServerProperties;
import dev.kacperjackiewicz.r2mod_sync.model.Health;
import dev.kacperjackiewicz.r2mod_sync.model.server.GameServer;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/server")
@RequiredArgsConstructor
public class GameServerApiController {

    private final GameServer gameServerControlService;
    private final GameServerProperties gameServerProperties;

    @GetMapping("/health")
    public Health health() {
        return gameServerControlService.getHealth();
    }

    @GetMapping("/config")
    public Map<String, String> config() {
        return Map.of("processName", gameServerProperties.processName());
    }

    @PostMapping("/start")
    public void start() {
        gameServerControlService.start();
    }

    @PostMapping("/stop")
    public void stop() {
        gameServerControlService.stop();
    }

    @PostMapping("/restart")
    public void restart() {
        gameServerControlService.restart();
    }
}
