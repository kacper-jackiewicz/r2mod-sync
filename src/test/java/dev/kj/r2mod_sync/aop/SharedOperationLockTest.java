package dev.kj.r2mod_sync.aop;

import dev.kj.r2mod_sync.exception.OperationInProgressException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SharedOperationLockTest {

    private final SharedOperationLock lock = new SharedOperationLock();

    @Test
    void acquiredLockRejectsAnotherOperationUntilReleased() {
        lock.tryAcquire();

        assertTrue(lock.isLocked());
        assertThrows(OperationInProgressException.class, lock::tryAcquire);

        lock.release();

        assertFalse(lock.isLocked());
        lock.tryAcquire();
        assertTrue(lock.isLocked());
    }
}
