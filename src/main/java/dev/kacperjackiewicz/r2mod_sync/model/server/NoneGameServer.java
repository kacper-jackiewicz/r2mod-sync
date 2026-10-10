package dev.kacperjackiewicz.r2mod_sync.model.server;

import dev.kacperjackiewicz.r2mod_sync.model.Health;
import dev.kacperjackiewicz.r2mod_sync.model.ServerStatus;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import java.util.concurrent.TimeUnit;

@AllArgsConstructor
public class NoneGameServer implements GameServer {

    private final String processName;
    private ServerStatus status;

    @Override
    public Health getHealth() {
        return new Health(processName, status);
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

}
