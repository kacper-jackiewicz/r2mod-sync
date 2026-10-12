package dev.kj.r2mod_sync.config;

import dev.kj.r2mod_sync.model.server.DockerGameServer;
import dev.kj.r2mod_sync.model.server.GameServer;
import dev.kj.r2mod_sync.model.server.NoneGameServer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class GameServerConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(GameServerConfiguration.class);

    @Test
    void createsDockerServerBeanWhenTypeIsDocker() {
        contextRunner
                .withPropertyValues(
                        "game-server.type=DOCKER",
                        "game-server.process-name=valheim",
                        "game-server.mod-directory=mods",
                        "game-server.status=RUNNING")
                .run(context -> {
                    assertThat(context).hasSingleBean(GameServer.class);
                    assertThat(context).hasSingleBean(DockerGameServer.class);
                    assertThat(context).doesNotHaveBean(NoneGameServer.class);
                });
    }

    @Test
    void createsNoneServerInsteadWhenTypeIsNone() {
        contextRunner
                .withPropertyValues(
                        "game-server.type=NONE",
                        "game-server.process-name=valheim",
                        "game-server.mod-directory=mods",
                        "game-server.status=RUNNING")
                .run(context -> {
                    assertThat(context).hasSingleBean(GameServer.class);
                    assertThat(context).hasSingleBean(NoneGameServer.class);
                    assertThat(context).doesNotHaveBean(DockerGameServer.class);
                });
    }

    @Test
    void doesNotCreateDockerServerWhenTypeIsMissing() {
        contextRunner
                .withPropertyValues(
                        "game-server.process-name=valheim",
                        "game-server.mod-directory=mods",
                        "game-server.status=RUNNING")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(DockerGameServer.class);
                    assertThat(context).doesNotHaveBean(GameServer.class);
                });
    }
}
