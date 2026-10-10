package dev.kacperjackiewicz.r2mod_sync.web;

import dev.kacperjackiewicz.r2mod_sync.service.GameServerControlService;
import dev.kacperjackiewicz.r2mod_sync.service.GameServerControlService.Health;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/server")
public class GameServerApiController {

    private final GameServerControlService gameServerControlService;

    public GameServerApiController(GameServerControlService gameServerControlService) {
        this.gameServerControlService = gameServerControlService;
    }

    @GetMapping("/health")
    public Health health() {
        return gameServerControlService.getHealth();
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
