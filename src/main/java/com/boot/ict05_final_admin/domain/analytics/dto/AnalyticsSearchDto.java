package com.boot.ict05_final_admin.domain.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

/**
 * 공통 분석 검색 조건을 담는 DTO.
 *
 * <p>가맹점 선택, 조회 기간(시작/종료), 일/월 단위 보기, 출력 개수, Total 표시 여부를 포함한다.</p>
 *
 * @author 이경욱
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsSearchDto {

    /** 가맹점 ID 목록(미지정 시 전체) */
    private List<Long> storeIds;

    /** 조회 시작일 (yyyy-MM-dd) */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /** 조회 종료일 (yyyy-MM-dd, 포함) */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    /** 보기 방식 (일/월) */
    private ViewBy viewBy = ViewBy.DAY;

    /** 출력 개수(테이블 상단 셀렉트와 연동) */
    private Integer limit = 50;

    /** 표에 Total 행을 표시할지 여부 (기본값: true) */
    private Boolean showTotal = true;

    /**
     * NPE 방지를 위한 기본값 주입 도우미.
     * <ul>
     *     <li>startDate: 해당 연도 1월 1일</li>
     *     <li>endDate: 오늘</li>
     *     <li>viewBy: DAY</li>
     *     <li>limit: 40</li>
     *     <li>showTotal: true</li>
     * </ul>
     *
     * @param in 원본 검색조건(Null 가능)
     * @return Null 안전한 검색조건 사본
     */
    public static AnalyticsSearchDto withDefaults(AnalyticsSearchDto in) {
        AnalyticsSearchDto s = (in == null) ? new AnalyticsSearchDto() : in;
        LocalDate today = LocalDate.now();

        if (s.getStartDate() == null) s.setStartDate(today.withDayOfYear(1));
        if (s.getEndDate() == null)   s.setEndDate(today);
        if (s.getViewBy() == null)    s.setViewBy(ViewBy.DAY);
        if (s.getLimit() == null)     s.setLimit(40);
        if (s.getShowTotal() == null) s.setShowTotal(true);

        return s;
    }
}
