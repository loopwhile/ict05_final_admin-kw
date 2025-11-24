package com.boot.ict05_final_admin.domain.analytics.dto;

import lombok.*;
import java.util.List;

/**
 * 시간·요일 분석 카드 섹션 DTO.
 *
 * <p>
 * 카드 UI 영역에서 사용하는 간략화된 시간/요일 라벨과 시리즈 데이터를 제공한다.
 * {@link TimeChartRowDto} 와 구조는 유사하지만, 카드용(요약) 차트에 최적화되어 사용된다.
 * </p>
 *
 * <h3>구성</h3>
 * <ul>
 *   <li>{@code hours}: 시간대 라벨(예: {@code ["08:00", ... , "22:00"]})</li>
 *   <li>{@code dows}: 요일 라벨(예: {@code ["일","월","화","수","목","금","토"]})</li>
 *   <li>{@code timeOfDay}: 시간대 기준 시리즈</li>
 *   <li>{@code dayOfWeek}: 요일 기준 시리즈</li>
 * </ul>
 *
 * @author ICT
 * @since 2025.10
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeChartCardDto {

    /**
     * 시간대 라벨 목록.
     * <p>예: {@code ["08:00", ..., "22:00"]}</p>
     */
    private List<String> hours;

    /**
     * 요일 라벨 목록.
     * <p>예: {@code ["일","월","화","수","목","금","토"]}</p>
     */
    private List<String> dows;

    /**
     * 시간대 기준 시리즈 목록.
     * <p>{@code hours} 라벨 순서와 동일한 인덱스 규칙을 따른다.</p>
     */
    private List<ChartSeriesDto> timeOfDay;

    /**
     * 요일 기준 시리즈 목록.
     * <p>{@code dows} 라벨 순서와 동일한 인덱스 규칙을 따른다.</p>
     */
    private List<ChartSeriesDto> dayOfWeek;
}
