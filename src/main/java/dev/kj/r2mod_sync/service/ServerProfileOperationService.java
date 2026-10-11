package dev.kj.r2mod_sync.service;

import dev.kj.r2mod_sync.aop.SharedOperationLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static dev.kj.r2mod_sync.service.FileService.CANDIDATE_PREFIX;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServerProfileOperationService {

    public enum OperationState {
        PENDING,
        COMPLETED,
        FAILED
    }

    private final SharedOperationLock sharedOperationLock;
    private final FileService fileService;
    private final UpdateService updateService;
    private final ExecutorService serverProfileExecutor;
    private final ConcurrentHashMap<UUID, OperationStatus> operations = new ConcurrentHashMap<>();


    public UUID submitModLoad(Supplier<Path> supplier, String responsePrefix) {
        sharedOperationLock.tryAcquire();
        try {
            return startOperation(supplier.get(), responsePrefix + ".success", responsePrefix + ".error");
        } catch (Exception exception) {
            sharedOperationLock.release();
            throw exception;
        }
    }

    public OperationStatus getStatus(UUID operationId) {
        return operations.get(operationId);
    }

    private UUID startOperation(Path candidate, String successKey, String validationErrorKey) {
        UUID operationId = UUID.randomUUID();
        operations.put(operationId, new OperationStatus(OperationState.PENDING, "server.operationInProgress"));
        try {
            serverProfileExecutor.execute(() -> runOperation(
                    operationId, candidate, successKey, validationErrorKey));
            return operationId;
        } catch (RuntimeException exception) {
            operations.remove(operationId);
            if (candidate.getFileName().toString().startsWith(CANDIDATE_PREFIX)) {
                fileService.deleteCandidate(candidate);
            }
            throw exception;
        }
    }

    private void runOperation(
            UUID operationId, Path candidate, String successKey, String validationErrorKey) {
        try {
            updateService.update(candidate);
            fileService.updateCurrent(candidate);
            operations.put(operationId, new OperationStatus(OperationState.COMPLETED, successKey));
        } catch (IllegalArgumentException exception) {
            operations.put(operationId, new OperationStatus(OperationState.FAILED, validationErrorKey));
        } catch (Exception exception) {
            log.error("Server profile operation {} failed.", operationId, exception);
            operations.put(operationId, new OperationStatus(OperationState.FAILED, "server.actionFailed"));
        } finally {
            if (candidate.getFileName().toString().startsWith(CANDIDATE_PREFIX)) {
                fileService.deleteCandidate(candidate);
            }
            sharedOperationLock.release();
            CompletableFuture.delayedExecutor(1, TimeUnit.HOURS)
                    .execute(() -> operations.remove(operationId));
        }
    }

    public record OperationStatus(OperationState state, String messageKey) {
    }
}
