package com.example.olca.global.trace;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Aspect
@Component
public class TraceLogAspect {

    @Around("@annotation(traceLog)")
    public Object trace(ProceedingJoinPoint joinPoint, TraceLog traceLog) throws Throwable {
        String methodName = methodName(joinPoint, traceLog);
        long start = System.currentTimeMillis();
        Object result;

        try {
            result = joinPoint.proceed();
        } catch (Exception e) {
            log.warn("[TRACE] traceId={} method={}(메서드) elapsedMs={}(소요시간ms) success=false(실패) error={}",
                    createTraceId(), methodName, elapsedMs(start), e.getClass().getSimpleName());
            throw e;
        }

        if (result instanceof Mono<?> mono) {
            return traceMono(mono, methodName);
        }

        if (result instanceof Flux<?> flux) {
            return traceFlux(flux, methodName);
        }

        log.info("[TRACE] traceId={} method={}(메서드) elapsedMs={}(소요시간ms) success=true(성공)",
                createTraceId(), methodName, elapsedMs(start));
        return result;
    }

    private Mono<?> traceMono(Mono<?> mono, String methodName) {
        return Mono.deferContextual(contextView -> {
            String traceId = contextView.getOrDefault(TraceKeys.TRACE_ID, createTraceId());
            long start = System.currentTimeMillis();

            return mono
                    .doOnSuccess(value -> log.info("[TRACE] traceId={} method={}(메서드) elapsedMs={}(소요시간ms) success=true(성공)",
                            traceId, methodName, elapsedMs(start)))
                    .doOnError(error -> log.warn("[TRACE] traceId={} method={}(메서드) elapsedMs={}(소요시간ms) success=false(실패) error={}",
                            traceId, methodName, elapsedMs(start), error.getClass().getSimpleName()));
        });
    }

    private Flux<?> traceFlux(Flux<?> flux, String methodName) {
        return Flux.deferContextual(contextView -> {
            String traceId = contextView.getOrDefault(TraceKeys.TRACE_ID, createTraceId());
            long start = System.currentTimeMillis();

            return flux
                    .doOnComplete(() -> log.info("[TRACE] traceId={} method={}(메서드) elapsedMs={}(소요시간ms) success=true(성공)",
                            traceId, methodName, elapsedMs(start)))
                    .doOnError(error -> log.warn("[TRACE] traceId={} method={}(메서드) elapsedMs={}(소요시간ms) success=false(실패) error={}",
                            traceId, methodName, elapsedMs(start), error.getClass().getSimpleName()));
        });
    }

    private String methodName(ProceedingJoinPoint joinPoint, TraceLog traceLog) {
        if (!traceLog.value().isBlank()) {
            return traceLog.value();
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getDeclaringType().getSimpleName() + "." + signature.getMethod().getName();
    }

    private long elapsedMs(long start) {
        return System.currentTimeMillis() - start;
    }

    private String createTraceId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
