package dev.kj.r2mod_sync.service;

import dev.kj.r2mod_sync.model.server.GameServer;
import dev.kj.r2mod_sync.service.mod.ModLoader;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

import static dev.kj.r2mod_sync.model.ServerStatus.RUNNING;
import static dev.kj.r2mod_sync.model.ServerStatus.STOPPED;
import static org.awaitility.Awaitility.await;

@Service
@RequiredArgsConstructor
public class UpdateService {

    private final GameServer gameServer;
    private final FileValidator fileValidator;
    private final ModLoader modLoader;

    @SneakyThrows
    public void update(Path candidate) {
        fileValidator.validateR2Z(candidate);
        gameServer.stop();
        await().until(() -> gameServer.getStatus() == STOPPED);
        modLoader.cleanModDir(gameServer.getModDirectory());
        modLoader.loadMods(candidate, gameServer.getModDirectory());
        gameServer.start();
        await().until(() -> gameServer.getStatus() == RUNNING);
    }
}
