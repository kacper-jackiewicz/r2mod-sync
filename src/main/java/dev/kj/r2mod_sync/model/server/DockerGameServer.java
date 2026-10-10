package dev.kj.r2mod_sync.model.server;

import dev.kj.r2mod_sync.model.ServerStatus;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
public class DockerGameServer implements GameServer {

    private final String containerName;
    private ServerStatus status = ServerStatus.STOPPED;

    @Override
    public ServerStatus getStatus() {
        return status;
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {

    }

    @Override
    public void restart() {

    }

    @Override
    public void block() {
        status = ServerStatus.UPDATING;
    }
}
