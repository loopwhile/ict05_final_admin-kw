// src/main/java/.../config/StringToLocalDateTimeConverter.java
package com.boot.ict05_final_admin.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
public class StringToLocalDateTimeConverter implements Converter<String, LocalDateTime> {
    @Override
    public LocalDateTime convert(String source) {
        if (source == null) return null;
        String s = source.trim();
        if (s.isEmpty()) return null;

        // 1) yyyy-MM-dd → 자정(00:00:00)
        if (s.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return LocalDate.parse(s).atStartOfDay();
        }
        // 2) yyyy-MM-ddTHH:mm 또는 yyyy-MM-dd HH:mm(:ss) 형태도 허용
        s = s.replace(' ', 'T'); // 스페이스로 올 때 보정
        try {
            return LocalDateTime.parse(s);
        } catch (Exception ignore) { /* fallthrough */ }

        // 3) 마지막 안전망: yyyy-MM-dd만 남아 있으면 자정
        try {
            return LocalDate.parse(source.trim()).atStartOfDay();
        } catch (Exception e) {
            throw new IllegalArgumentException("날짜/시간 형식 오류: " + source);
        }
    }
}
