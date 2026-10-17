package dev.kj.r2mod_sync.aop;

import dev.kj.r2mod_sync.exception.OperationInProgressException;
import org.springframework.stereotype.Component;

import java.util.concurrent.Semaphore;

@Component
public class SharedOperationLock {

    private final Semaphore semaphore = new Semaphore(1);

    public void tryAcquire() {
        if (!semaphore.tryAcquire()) {
            throw new OperationInProgressException();
        }
    }

    public void release() {
        semaphore.release();
    }

    public boolean isLocked() {
        return semaphore.availablePermits() == 0;
    }
}
