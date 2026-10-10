package dev.kj.r2mod_sync.aop;

import dev.kj.r2mod_sync.exception.ServerNotRunningException;
import dev.kj.r2mod_sync.model.ServerStatus;
import dev.kj.r2mod_sync.model.server.GameServer;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class StateOperationAdvice {

    private final GameServer gameServer;

    @Around("@annotation(dev.kj.r2mod_sync.aop.StateOperation)")
    public Object checkLock(ProceedingJoinPoint joinPoint) throws Throwable {
        if (gameServer.getStatus() != ServerStatus.RUNNING) {
            throw new ServerNotRunningException();
        }
        return joinPoint.proceed();
    }
}
