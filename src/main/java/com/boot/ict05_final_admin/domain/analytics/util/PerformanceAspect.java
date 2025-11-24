package com.boot.ict05_final_admin.domain.analytics.util;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Aspect
@Component
@Slf4j
public class PerformanceAspect {

    @Around("@annotation(LogExecutionTime)")
    public Object logTimeAndCount(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long end = System.currentTimeMillis();
        long duration = end - start;

        int rowCount = -1;
        if (result instanceof Page<?>) {
            rowCount = (int) ((Page<?>) result).getTotalElements();
        } else if (result instanceof Collection<?>) {
            rowCount = ((Collection<?>) result).size();
        } else if (result != null) {
            rowCount = 1; // 단일 DTO 또는 단일 객체
        } else {
            rowCount = 0; // null 반환 시
        }

        log.info("[PERF] {} 실행시간: {} ms, 결과 Row 수: {}",
                joinPoint.getSignature(), duration, rowCount);

        return result;
    }
}