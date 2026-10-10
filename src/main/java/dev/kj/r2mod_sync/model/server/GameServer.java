package dev.kj.r2mod_sync.model.server;

import dev.kj.r2mod_sync.model.ServerStatus;

public interface GameServer {
    ServerStatus getStatus();

    void start();

    void stop();

    void restart();

    void block();

}
