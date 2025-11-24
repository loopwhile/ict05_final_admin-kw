package com.boot.ict05_final_admin.domain.inventory.utility;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;

/**
 * 엑셀 변환 보조 유틸리티.
 *
 * <p>엑셀 내보내기 시 널 안전 문자열/숫자 변환을 제공한다.</p>
 *
 * <ul>
 *   <li>{@link #n(String)}: null → 빈 문자열("")</li>
 *   <li>{@link #s(Object)}: null → 빈 문자열(""), 그 외 {@code String.valueOf}</li>
 *   <li>{@link #d(Number)}: null → 0d, {@link BigDecimal}은 {@code doubleValue()}로 변환</li>
 * </ul>
 *
 * <p><b>주의</b>: {@code d(Number)}는 소수 정밀도가 중요한 금액/수량의
 * 가공에는 적합하지 않다(부동소수 변환). 시각화/표시 목적(예: XLSX 셀 값)으로 사용한다.</p>
 *
 * @author 김주연
 * @since 2025-11-12
 */
@UtilityClass
public class ExcelUtil {

    /**
     * null-safe 문자열 반환.
     *
     * @param v 원본 문자열
     * @return {@code v}가 null이면 빈 문자열, 아니면 원본
     */
    public static String n(String v) {
        return v == null ? "" : v;
    }

    /**
     * null-safe 객체 → 문자열 변환.
     *
     * @param v 임의의 객체
     * @return {@code v}가 null이면 빈 문자열, 아니면 {@code String.valueOf(v)}
     */
    public static String s(Object v) {
        return v == null ? "" : String.valueOf(v);
    }

    /**
     * null-safe 숫자 → double 변환.
     *
     * <p>{@link BigDecimal}은 {@code doubleValue()}로, 그 외 {@link Number#doubleValue()} 사용.</p>
     *
     * @param v 숫자 인스턴스
     * @return {@code v}가 null이면 0d, 아니면 double 값
     */
    public static double d(Number v) {
        if (v == null) return 0d;
        if (v instanceof BigDecimal bd) return bd.doubleValue();
        return v.doubleValue();
    }
}
