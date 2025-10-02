package com.amsidh.mvc.common.logging;

import java.util.UUID;

import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;

/**
 * Generic Aspect that logs every public method invocation of classes annotated with @Service.
 * It logs method entry/exit, arguments, duration, and a trackingId (traceId if available).
 */
@Slf4j
@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 100)
@ConditionalOnProperty(name = "common-util.logging.service.enabled", havingValue = "true", matchIfMissing = true)
public class ServiceMethodLoggingAspect {

    private final Tracer tracer;

    public ServiceMethodLoggingAspect(Tracer tracer) {
        this.tracer = tracer;
    }

    // Any class annotated with @Service
    @Pointcut("within(@org.springframework.stereotype.Service *)")
    public void serviceClass() {}

    // Any public method execution
    @Pointcut("execution(public * *(..))")
    public void publicMethod() {}

    @Around("serviceClass() && publicMethod()")
    public Object logAround(org.aspectj.lang.ProceedingJoinPoint pjp) throws Throwable {
        String trackingId = resolveTrackingId();

        String className = pjp.getSignature().getDeclaringTypeName();
        String methodName = pjp.getSignature().getName();
        Object[] args = pjp.getArgs();

        long start = System.currentTimeMillis();
        log.info("[SERVICE] ENTER {}.{} args={} trackingId={}", className, methodName, safeArgs(args), trackingId);

        try {
            Object result = pjp.proceed();
            long took = System.currentTimeMillis() - start;
            log.info("[SERVICE] EXIT  {}.{} took={}ms trackingId={} result={}", className, methodName, took, trackingId, summarizeResult(result));
            return result;
        } catch (Throwable ex) {
            long took = System.currentTimeMillis() - start;
            log.error("[SERVICE] ERROR {}.{} took={}ms trackingId={} ex={}", className, methodName, took, trackingId, ex.toString());
            throw ex;
        }
    }

    private String resolveTrackingId() {
        try {
            if (tracer != null && tracer.currentTraceContext() != null) {
                var span = tracer.currentSpan();
                if (span != null) {
                    var ctx = span.context();
                    if (ctx != null) {
                        String id = ctx.traceId();
                        if (id != null && !id.isEmpty()) return id;
                    }
                }
            }
        } catch (Exception ignored) {
            // Best-effort; fall back to random tracking id below
        }
        return UUID.randomUUID().toString();
    }

    private Object safeArgs(Object[] args) {
        if (args == null) return null;
        Object[] copy = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            copy[i] = maskIfSensitive(args[i]);
        }
        return java.util.Arrays.asList(copy);
    }

    private Object maskIfSensitive(Object arg) {
        if (arg == null) return null;
        String s = String.valueOf(arg).toLowerCase();
        if (s.contains("password") || s.contains("secret") || s.contains("token")) {
            return "***MASKED***";
        }
        return arg;
    }

    private Object summarizeResult(Object result) {
        if (result == null) return null;
        String s = String.valueOf(result);
        if (s.length() > 300) {
            return s.substring(0, 300) + "...";
        }
        return result;
    }
}
