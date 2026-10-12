package dev.kj.r2mod_sync.aop;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class OperationAquireLockAdvice {

    private final SharedOperationLock sharedOperationLock;

    @Around("@annotation(dev.kj.r2mod_sync.aop.AquireLock)")
    public Object checkLock(ProceedingJoinPoint joinPoint) throws Throwable {
        sharedOperationLock.tryAcquire();
        try {
            return joinPoint.proceed(joinPoint.getArgs());
        } finally {
            sharedOperationLock.release();
        }
    }
}
