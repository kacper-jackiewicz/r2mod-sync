package dev.kj.r2mod_sync.model.server;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse.ContainerState;
import com.github.dockerjava.api.model.Container;
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
    private final DockerClient dockerClient;

    @Override
    public ServerStatus getStatus() {
        return getContainerId(dockerClient)
                .map(this::getStatusFromContainer)
                .orElse(ServerStatus.NOT_CONFIGURED);
    }

    @Override
    public void start() {
        getContainerId(dockerClient).ifPresent(containerId -> {
            log.info("Starting container {} with id {}",containerName, containerId);
            dockerClient.startContainerCmd(containerId).exec();
            log.info("Started container {} with id {}",containerName, containerId);
        });
    }

    @Override
    public void stop() {
        getContainerId(dockerClient).ifPresent(containerId -> {
            log.info("Stopping container {} with id {}",containerName, containerId);
            dockerClient.stopContainerCmd(containerId).exec();
            log.info("Stopped container {} with id {}",containerName, containerId);
        });
    }

    @Override
    public void restart() {
        getContainerId(dockerClient).ifPresent(containerId -> {
            log.info("Restarting container {} with id {}",containerName, containerId);
            dockerClient.restartContainerCmd(containerId).exec();
            log.info("Restarted container {} with id {}",containerName, containerId);
        });
    }

    @Override
    public Path getModDirectory() {
        return modDirectory;
    }

    private ServerStatus getStatusFromContainer(String containerId) {
        ContainerState state = dockerClient.inspectContainerCmd(containerId).exec().getState();

        if (Boolean.TRUE.equals(state.getRunning())) {
            return state.getHealth().getStatus().equals("healthy") ? ServerStatus.RUNNING : ServerStatus.UPDATING;
        } else {
            return ServerStatus.STOPPED;
        }
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
}
