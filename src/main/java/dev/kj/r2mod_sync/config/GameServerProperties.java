package dev.kj.r2mod_sync.config;

import dev.kj.r2mod_sync.model.ServerStatus;
import dev.kj.r2mod_sync.model.ServerType;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "game-server")
public record GameServerProperties(String processName, ServerType type, ServerStatus status) {
}
