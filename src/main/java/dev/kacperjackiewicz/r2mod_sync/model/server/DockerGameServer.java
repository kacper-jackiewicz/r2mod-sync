package dev.kacperjackiewicz.r2mod_sync.model.server;

import dev.kacperjackiewicz.r2mod_sync.model.Health;
import dev.kacperjackiewicz.r2mod_sync.model.ServerStatus;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DockerGameServer implements GameServer {

    private final String containerName;

    @Override
    public Health getHealth() {
        return new Health("Docker", ServerStatus.RUNNING);
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
}
