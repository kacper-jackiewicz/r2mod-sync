package dev.kj.r2mod_sync.aop;

import dev.kj.r2mod_sync.exception.OperationInProgressException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.ReentrantLock;

@Aspect
@Component
public class OperationAquireLockAdvice {

    private final ReentrantLock lock = new ReentrantLock();

    @Around("@annotation(dev.kj.r2mod_sync.aop.AquireLock)")
    public Object checkLock(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!lock.tryLock()) {
            throw new OperationInProgressException();
        }
        try {
            return joinPoint.proceed(joinPoint.getArgs());
        } finally {
            lock.unlock();
        }
    }
}
