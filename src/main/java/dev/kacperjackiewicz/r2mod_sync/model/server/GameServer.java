package dev.kacperjackiewicz.r2mod_sync.model.server;

import dev.kacperjackiewicz.r2mod_sync.model.Health;

public interface GameServer {
    Health getHealth();

    void start();

    void stop();

    void restart();

}
