package dev.kj.r2mod_sync.model.server;

import dev.kj.r2mod_sync.model.ServerStatus;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
public class NoneGameServer implements GameServer {

    private final Path modDirectory;
    private ServerStatus status;

    @Override
    public ServerStatus getStatus() {
        return status;
    }

    @Override
    public void start() {
        status = ServerStatus.RUNNING;
    }

    @Override
    public void stop() {
        status = ServerStatus.STOPPED;
    }

    @Override
    @SneakyThrows
    public void restart() {
        stop();
        TimeUnit.SECONDS.sleep(20);
        start();
    }

    @Override
    public Path getModDirectory() {
        return modDirectory;
    }

}
