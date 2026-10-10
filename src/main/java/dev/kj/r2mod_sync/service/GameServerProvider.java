package dev.kj.r2mod_sync.service;

import dev.kj.r2mod_sync.config.GameServerProperties;
import dev.kj.r2mod_sync.model.server.DockerGameServer;
import dev.kj.r2mod_sync.model.server.GameServer;
import dev.kj.r2mod_sync.model.server.NoneGameServer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class GameServerProvider {

    private final GameServerProperties gameServerProperties;

    @Bean
    public GameServer getGameServer() {
        return switch (gameServerProperties.type()) {
            case DOCKER -> new DockerGameServer(gameServerProperties.processName(),gameServerProperties.modDirectory());
            default -> new NoneGameServer(gameServerProperties.modDirectory(),gameServerProperties.status());
        };
    }

}
