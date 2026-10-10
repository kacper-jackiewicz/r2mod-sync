package dev.kj.r2mod_sync.service;

import dev.kj.r2mod_sync.model.server.GameServer;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UpdateService {

    private final GameServer gameServer;

    @SneakyThrows
    public void update(Path destination) {
        gameServer.block();
        TimeUnit.SECONDS.sleep(30);
        Files.delete(destination);
        gameServer.start();
    }
}
