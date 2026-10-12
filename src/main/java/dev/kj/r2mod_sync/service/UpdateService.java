package dev.kj.r2mod_sync.service;

import dev.kj.r2mod_sync.model.server.GameServer;
import dev.kj.r2mod_sync.service.mod.ModLoader;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.Duration;

import static dev.kj.r2mod_sync.model.ServerStatus.RUNNING;
import static dev.kj.r2mod_sync.model.ServerStatus.STOPPED;
import static org.awaitility.Awaitility.await;

@Service
@Slf4j
@RequiredArgsConstructor
public class UpdateService {

    private final GameServer gameServer;
    private final FileValidator fileValidator;
    private final ModLoader modLoader;

    @SneakyThrows
    public void update(Path candidate) {
        fileValidator.validateR2Z(candidate);
        gameServer.stop();
        log.info("Waiting for server to stop");
        await().atMost(Duration.ofMinutes(5)).until(() -> gameServer.getStatus() == STOPPED);
        modLoader.cleanModDir(gameServer.getModDirectory());
        modLoader.loadMods(candidate, gameServer.getModDirectory());
        gameServer.start();
        log.info("Waiting for server to start");
        await().atMost(Duration.ofMinutes(10)).until(() -> gameServer.getStatus() == RUNNING);
    }
}
