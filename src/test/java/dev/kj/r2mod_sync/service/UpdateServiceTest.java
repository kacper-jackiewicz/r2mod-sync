package dev.kj.r2mod_sync.service;

import dev.kj.r2mod_sync.model.ServerStatus;
import dev.kj.r2mod_sync.model.server.GameServer;
import dev.kj.r2mod_sync.service.mod.ModLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateServiceTest {

    @Mock
    private GameServer gameServer;
    @Mock
    private FileValidator fileValidator;
    @Mock
    private ModLoader modLoader;

    private UpdateService createService() {
        return new UpdateService(gameServer, fileValidator, modLoader);
    }

    @Test
    void updateValidatesStopsLoadsModsAndStartsServerInOrder() {
        Path candidate = Path.of("profile.r2z");
        Path modDirectory = Path.of("server", "mods");
        when(gameServer.getStatus()).thenReturn(ServerStatus.STOPPED, ServerStatus.RUNNING);
        when(gameServer.getModDirectory()).thenReturn(modDirectory);

        createService().update(candidate);

        InOrder order = inOrder(fileValidator, gameServer, modLoader);
        order.verify(fileValidator).validateR2Z(candidate);
        order.verify(gameServer).stop();
        order.verify(gameServer).getStatus();
        order.verify(gameServer).getModDirectory();
        order.verify(modLoader).cleanModDir(modDirectory);
        order.verify(gameServer).getModDirectory();
        order.verify(modLoader).loadMods(candidate, modDirectory);
        order.verify(gameServer).start();
        order.verify(gameServer).getStatus();
    }

    @Test
    void validationFailureLeavesServerAndModsUntouched() {
        Path candidate = Path.of("invalid.r2z");
        IllegalArgumentException validationFailure = new IllegalArgumentException("invalid profile");
        org.mockito.Mockito.doThrow(validationFailure).when(fileValidator).validateR2Z(candidate);

        assertThrows(IllegalArgumentException.class, () -> createService().update(candidate));

        verify(gameServer, never()).stop();
        verify(gameServer, never()).start();
        verify(modLoader, never()).cleanModDir(org.mockito.ArgumentMatchers.any());
        verify(modLoader, never()).loadMods(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }
}
