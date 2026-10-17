package dev.kj.r2mod_sync.service;

import dev.kj.r2mod_sync.aop.SharedOperationLock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import static dev.kj.r2mod_sync.service.ServerProfileOperationService.OperationState.COMPLETED;
import static dev.kj.r2mod_sync.service.ServerProfileOperationService.OperationState.FAILED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServerProfileOperationServiceTest {

    @Mock
    private SharedOperationLock sharedOperationLock;
    @Mock
    private FileService fileService;
    @Mock
    private UpdateService updateService;
    @Mock
    private ExecutorService executor;

    private ServerProfileOperationService service;

    @BeforeEach
    void setUp() {
        service = new ServerProfileOperationService(sharedOperationLock, fileService, updateService, executor);
    }

    private void executeOperationsImmediately() {
        doAnswer(invocation -> {
            invocation.<Runnable>getArgument(0).run();
            return null;
        }).when(executor).execute(any(Runnable.class));
    }

    @Test
    void successfulOperationUpdatesCurrentProfileAndReleasesLock() {
        Path candidate = Path.of("candidate_upload.r2z");
        executeOperationsImmediately();

        UUID operationId = service.submitModLoad(() -> candidate, "upload");

        assertEquals(new ServerProfileOperationService.OperationStatus(COMPLETED, "upload.success"),
                service.getStatus(operationId));
        verify(updateService).update(candidate);
        verify(fileService).updateCurrent(candidate);
        verify(fileService).deleteCandidate(candidate);
        verify(sharedOperationLock).release();
    }

    @Test
    void validationFailureUsesOperationSpecificErrorAndReleasesLock() {
        Path candidate = Path.of("candidate_upload.r2z");
        executeOperationsImmediately();
        doThrow(new IllegalArgumentException("invalid profile")).when(updateService).update(candidate);

        UUID operationId = service.submitModLoad(() -> candidate, "upload");

        assertEquals(new ServerProfileOperationService.OperationStatus(FAILED, "upload.error"),
                service.getStatus(operationId));
        verify(fileService, never()).updateCurrent(candidate);
        verify(fileService).deleteCandidate(candidate);
        verify(sharedOperationLock).release();
    }

    @Test
    void unexpectedFailureUsesGenericErrorAndReleasesLock() {
        Path candidate = Path.of("vanilla.r2z");
        executeOperationsImmediately();
        doThrow(new IllegalStateException("update failed")).when(updateService).update(candidate);

        UUID operationId = service.submitModLoad(() -> candidate, "restore");

        assertEquals(new ServerProfileOperationService.OperationStatus(FAILED, "server.actionFailed"),
                service.getStatus(operationId));
        verify(fileService, never()).deleteCandidate(candidate);
        verify(sharedOperationLock).release();
    }

    @Test
    void candidateSupplierFailureReleasesLockAndDoesNotStartOperation() {
        IllegalArgumentException failure = new IllegalArgumentException("upload failed");

        assertThrows(IllegalArgumentException.class,
                () -> service.submitModLoad(() -> {
                    throw failure;
                }, "upload"));

        verify(executor, never()).execute(any(Runnable.class));
        verify(sharedOperationLock).release();
    }

    @Test
    void executorRejectionCleansCandidateAndReleasesLock() {
        Path candidate = Path.of("candidate_upload.r2z");
        doThrow(new IllegalStateException("executor stopped")).when(executor).execute(any(Runnable.class));

        assertThrows(IllegalStateException.class, () -> service.submitModLoad(() -> candidate, "upload"));

        verify(fileService).deleteCandidate(candidate);
        verify(sharedOperationLock).release();
    }
}
