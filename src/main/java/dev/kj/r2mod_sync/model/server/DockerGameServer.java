package dev.kj.r2mod_sync.model.server;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import dev.kj.r2mod_sync.model.ServerStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class DockerGameServer implements GameServer {

    private final String containerName;
    private final Path modDirectory;

    private final DockerClient dockerClient = createDockerClient();

    @Override
    public ServerStatus getStatus() {
        return getContainerId(dockerClient)
                .map(this::getStatusFromContainer)
                .orElse(ServerStatus.NOT_CONFIGURED);
    }

    @Override
    public void start() {
            getContainerId(dockerClient).ifPresent(containerId -> {
                dockerClient.startContainerCmd(containerId).exec();
            });
    }

    @Override
    public void stop() {
        getContainerId(dockerClient).ifPresent(containerId -> {
            dockerClient.stopContainerCmd(containerId).exec();
        });
    }

    @Override
    public void restart() {
        getContainerId(dockerClient).ifPresent(containerId -> {
            dockerClient.restartContainerCmd(containerId).exec();
        });
    }

    @Override
    public Path getModDirectory() {
        return modDirectory;
    }

    private ServerStatus getStatusFromContainer(String containerId){
        return Boolean.TRUE.equals(dockerClient.inspectContainerCmd(containerId).exec().getState().getRunning()) ?ServerStatus.RUNNING:ServerStatus.STOPPED;
    }


    private Optional<String> getContainerId(DockerClient dockerClient) {
        return dockerClient.listContainersCmd()
                .withShowAll(true)
                .exec()
                .stream()
                .filter(container ->
                        List.of(container.getNames())
                                .contains("/" + containerName))
                .map(Container::getId)
                .findFirst();
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
