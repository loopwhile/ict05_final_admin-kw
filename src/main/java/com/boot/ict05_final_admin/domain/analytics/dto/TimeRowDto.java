package com.boot.ict05_final_admin.domain.analytics.dto;

import lombok.*;
import java.math.BigDecimal;

/**
 * 시간·요일 분석 테이블 행 DTO.
 *
 * <p>
 * 주문 행 기반의 시간·요일 분석 테이블 한 행을 표현한다.
 * 주문 페이지의 행 구조와 일관성을 위해 {@code orderType}, {@code orderDate}는 문자열로 유지한다.
 * </p>
 *
 * <h3>라벨 규칙</h3>
 * <ul>
 *   <li>{@code date}: 일별은 {@code yyyy-MM-dd}, 월별은 {@code yyyy-MM}</li>
 *   <li>{@code hourSlot}: {@code "HH:00-HH:59"} 형식</li>
 *   <li>{@code dayOfWeek}: {@code "일" ~ "토"}</li>
 * </ul>
 *
 * @author ICT
 * @since 2025.10
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeRowDto {

    /**
     * 기간 라벨.
     * <p>일별: {@code yyyy-MM-dd}, 월별: {@code yyyy-MM}</p>
     */
    private String date;

    /** 점포명. */
    private String storeName;

    /**
     * 시간대 구간.
     * <p>예: {@code "17:00-17:59"}</p>
     */
    private String hourSlot;

    /**
     * 요일 라벨.
     * <p>예: {@code "일"}, {@code "월"}, ... , {@code "토"}</p>
     */
    private String dayOfWeek;

    /** 주문 ID. */
    private Long orderId;

    /** 주문 금액. */
    private BigDecimal orderAmount;

    /** 메뉴 카테고리명. */
    private String category;

    /** 메뉴명. */
    private String menu;

    /**
     * 주문 유형.
     * <p>예: {@code "VISIT"}, {@code "TAKEOUT"}, {@code "DELIVERY"}</p>
     */
    private String orderType;

    /**
     * 주문 일시 문자열.
     * <p>예: {@code "yyyy-MM-dd HH:mm"}</p>
     */
    private String orderDate;
}
