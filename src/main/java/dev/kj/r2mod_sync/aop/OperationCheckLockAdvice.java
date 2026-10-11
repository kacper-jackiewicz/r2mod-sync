package dev.kj.r2mod_sync.aop;

import dev.kj.r2mod_sync.exception.OperationInProgressException;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class OperationCheckLockAdvice {

    private final SharedOperationLock sharedOperationLock;

    @Around("@annotation(dev.kj.r2mod_sync.aop.CheckLock)")
    public Object checkLock(ProceedingJoinPoint joinPoint) throws Throwable {
        if (sharedOperationLock.isLocked()) {
            throw new OperationInProgressException();
        }
        return joinPoint.proceed(joinPoint.getArgs());
    }
}
