package com.boot.ict05_final_admin.domain.analytics.dto;

import lombok.*;
import java.util.List;

/**
 * 시간·요일 분석 차트 행 DTO.
 *
 * <p>
 * 시간대/요일 라벨과 각 라벨에 대응하는 시리즈 데이터(필터 적용)를 함께 전달한다.
 * 프런트엔드 차트 구성 시 X축 라벨과 시리즈 값의 인덱스 정합성을 보장한다.
 * </p>
 *
 * <h3>구성</h3>
 * <ul>
 *   <li>{@code hours}: 시간대 라벨(예: {@code ["08:00", ... , "22:00"]})</li>
 *   <li>{@code dows}: 요일 라벨(예: {@code ["일","월","화","수","목","금","토"]})</li>
 *   <li>{@code timeOfDay}: 시간대 기준 시리즈(필터 적용)</li>
 *   <li>{@code dayOfWeek}: 요일 기준 시리즈(필터 적용)</li>
 * </ul>
 *
 * @author ICT
 * @since 2025.10
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TimeChartRowDto {

    /**
     * 시간대 라벨 목록.
     * <p>예: {@code ["08:00", "09:00", ..., "22:00"]}</p>
     */
    private List<String> hours;

    /**
     * 요일 라벨 목록.
     * <p>예: {@code ["일","월","화","수","목","금","토"]}</p>
     */
    private List<String> dows;

    /**
     * 시간대 기준 시리즈 목록(필터 적용).
     * <p>{@code hours} 라벨 순서와 동일한 인덱스 규칙을 따른다.</p>
     */
    private List<ChartSeriesDto> timeOfDay;

    /**
     * 요일 기준 시리즈 목록(필터 적용).
     * <p>{@code dows} 라벨 순서와 동일한 인덱스 규칙을 따른다.</p>
     */
    private List<ChartSeriesDto> dayOfWeek;
}
