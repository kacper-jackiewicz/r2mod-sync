package dev.kj.r2mod_sync.aop;

import java.util.concurrent.locks.ReentrantLock;

import dev.kj.r2mod_sync.exception.OperationInProgressException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class OperationCheckLockAdvice {

    private final ReentrantLock lock = new ReentrantLock();

    @Around("@annotation(dev.kj.r2mod_sync.aop.CheckLock)")
    public Object checkLock(ProceedingJoinPoint joinPoint) throws Throwable {
        if (lock.isLocked()) {
            throw new OperationInProgressException();
        }
        return joinPoint.proceed(joinPoint.getArgs());
    }
}
