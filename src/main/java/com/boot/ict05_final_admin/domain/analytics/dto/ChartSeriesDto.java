package com.boot.ict05_final_admin.domain.analytics.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * 차트 시리즈 DTO.
 *
 * <p>
 * 하나의 시리즈(예: "Total - VISIT", "강남점 - DELIVERY")와
 * 라벨 순서에 매칭되는 데이터 배열을 표현한다.
 * </p>
 *
 * <h3>구성</h3>
 * <ul>
 *   <li>{@code name}: 시리즈 표시명(범례에 노출)</li>
 *   <li>{@code data}: 라벨(시간/요일 등) 순서에 대응되는 값 목록</li>
 * </ul>
 *
 * @author ICT
 * @since 2025.10
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ChartSeriesDto {

    /**
     * 시리즈 이름.
     * <p>예: {@code "Total - VISIT"}, {@code "강남점 - DELIVERY"}</p>
     */
    private String name;

    /**
     * 라벨 순서에 매칭되는 값 목록.
     * <p>시간대/요일/기간 라벨과 동일한 인덱스 순서를 유지해야 한다.</p>
     */
    private List<BigDecimal> data;
}
