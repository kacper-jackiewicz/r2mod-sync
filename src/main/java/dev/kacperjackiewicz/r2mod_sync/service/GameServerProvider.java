package dev.kacperjackiewicz.r2mod_sync.service;

import dev.kacperjackiewicz.r2mod_sync.config.GameServerProperties;
import dev.kacperjackiewicz.r2mod_sync.model.server.DockerGameServer;
import dev.kacperjackiewicz.r2mod_sync.model.server.GameServer;
import dev.kacperjackiewicz.r2mod_sync.model.server.NoneGameServer;
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
            case DOCKER -> new DockerGameServer(gameServerProperties.processName());
            default -> new NoneGameServer(gameServerProperties.processName(),gameServerProperties.status());
        };
    }

}
