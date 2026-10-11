package dev.kj.r2mod_sync.model.server;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.Container;
import dev.kj.r2mod_sync.model.ServerStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DockerGameServerTest {

    private static final String CONTAINER_ID = "container-id";
    private static final String CONTAINER_NAME = "valheim";

    @Mock
    private DockerClient dockerClient;
    @Mock
    private ListContainersCmd listContainersCmd;
    @Mock
    private Container container;

    private DockerGameServer server;

    @BeforeEach
    void setUp() {
        server = new DockerGameServer(CONTAINER_NAME, Path.of("mods"), dockerClient);
        when(dockerClient.listContainersCmd()).thenReturn(listContainersCmd);
        when(listContainersCmd.withShowAll(true)).thenReturn(listContainersCmd);
    }

    @Test
    void statusIsNotConfiguredWhenNamedContainerIsMissing() {
        when(listContainersCmd.exec()).thenReturn(List.of());

        assertEquals(ServerStatus.NOT_CONFIGURED, server.getStatus());
    }

    @Test
    void statusIsStoppedWhenContainerIsNotRunning() {
        InspectContainerResponse inspection = inspection(false, null);
        mockContainerLookup(inspection);

        assertEquals(ServerStatus.STOPPED, server.getStatus());
    }

    @Test
    void statusIsRunningWhenContainerHealthIsHealthy() {
        InspectContainerResponse inspection = inspection(true, "healthy");
        mockContainerLookup(inspection);

        assertEquals(ServerStatus.RUNNING, server.getStatus());
    }

    @Test
    void statusIsUpdatingWhenContainerHealthIsNotHealthy() {
        InspectContainerResponse inspection = inspection(true, "starting");
        mockContainerLookup(inspection);

        assertEquals(ServerStatus.UPDATING, server.getStatus());
    }

    @Test
    void lifecycleCommandsTargetMatchingContainer() {
        mockContainerLookup(null);
        StartContainerCmd startCommand = org.mockito.Mockito.mock(StartContainerCmd.class);
        StopContainerCmd stopCommand = org.mockito.Mockito.mock(StopContainerCmd.class);
        RestartContainerCmd restartCommand = org.mockito.Mockito.mock(RestartContainerCmd.class);
        when(dockerClient.startContainerCmd(CONTAINER_ID)).thenReturn(startCommand);
        when(dockerClient.stopContainerCmd(CONTAINER_ID)).thenReturn(stopCommand);
        when(dockerClient.restartContainerCmd(CONTAINER_ID)).thenReturn(restartCommand);

        server.start();
        server.stop();
        server.restart();

        verify(startCommand).exec();
        verify(stopCommand).exec();
        verify(restartCommand).exec();
    }

    private void mockContainerLookup(InspectContainerResponse inspection) {
        when(container.getNames()).thenReturn(new String[]{"/" + CONTAINER_NAME});
        when(container.getId()).thenReturn(CONTAINER_ID);
        when(listContainersCmd.exec()).thenReturn(List.of(container));
        if (inspection != null) {
            InspectContainerCmd inspectCommand = org.mockito.Mockito.mock(InspectContainerCmd.class);
            when(dockerClient.inspectContainerCmd(CONTAINER_ID)).thenReturn(inspectCommand);
            when(inspectCommand.exec()).thenReturn(inspection);
        }
    }

    private InspectContainerResponse inspection(boolean running, String healthStatus) {
        InspectContainerResponse response = org.mockito.Mockito.mock(InspectContainerResponse.class);
        InspectContainerResponse.ContainerState state =
                org.mockito.Mockito.mock(InspectContainerResponse.ContainerState.class);
        when(response.getState()).thenReturn(state);
        when(state.getRunning()).thenReturn(running);
        if (running) {
            HealthState health = org.mockito.Mockito.mock(HealthState.class);
            when(state.getHealth()).thenReturn(health);
            when(health.getStatus()).thenReturn(healthStatus);
        }
        return response;
    }
}
