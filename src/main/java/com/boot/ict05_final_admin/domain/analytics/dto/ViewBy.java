package com.boot.ict05_final_admin.domain.analytics.dto;

/**
 * 조회 결과의 집계 단위를 지정하는 열거형.
 *
 * <p>일별(DAY) 또는 월별(MONTH)로 집계 단위를 선택한다.</p>
 */
public enum ViewBy {

    /** 일별 집계 */
    DAY("일별"),

    /** 월별 집계 */
    MONTH("월별");

    /** 한글 설명 */
    private final String description;

    /**
     * 생성자.
     *
     * @param description 한글 설명
     */
    ViewBy(String description) {
        this.description = description;
    }

    /**
     * 한글 설명을 반환한다.
     *
     * @return 한글 설명
     */
    public String getDescription() {
        return description;
    }
}
