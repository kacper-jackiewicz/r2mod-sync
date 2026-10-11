package dev.kj.r2mod_sync.config;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import dev.kj.r2mod_sync.model.server.DockerGameServer;
import dev.kj.r2mod_sync.model.server.GameServer;
import dev.kj.r2mod_sync.model.server.NoneGameServer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(GameServerProperties.class)
public class GameServerConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "game-server", name = "type", havingValue = "DOCKER")
    public GameServer dockerGameServer(GameServerProperties properties) {
        return new DockerGameServer(properties.processName(), properties.modDirectory(), createDockerClient());
    }

    @Bean
    @ConditionalOnProperty(prefix = "game-server", name = "type", havingValue = "NONE")
    public GameServer noneGameServer(GameServerProperties properties) {
        return new NoneGameServer(properties.modDirectory(), properties.status());
    }

    private static DockerClient createDockerClient() {
        var config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        var httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .build();
        return DockerClientBuilder.getInstance(config)
                .withDockerHttpClient(httpClient)
                .build();
    }
}
