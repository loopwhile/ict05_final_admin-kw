package com.boot.ict05_final_admin.domain.notice.entity;

/**
 * 공지사항 카테고리 Enum
 *
 * <p>공지사항의 분류를 정의하며, 각 항목은 한글 설명(description)을 가진다.</p>
 *
 * <p>주요 카테고리:</p>
 * <ul>
 *     <li>NORMAL: 일반 공지</li>
 *     <li>SYSTEM: 시스템 관련 공지</li>
 *     <li>EVENT: 이벤트 공지</li>
 *     <li>POLICY: 정책/공지사항</li>
 * </ul>
 */
public enum NoticeCategory {

    /** 일반 공지 */
    NORMAL("일반"),

    /** 시스템 관련 공지 */
    SYSTEM("시스템"),

    /** 이벤트 공지 */
    EVENT("이벤트"),

    /** 정책/공지사항 */
    POLICY("공지");

    /** 한글 설명 */
    private final String description;

    /**
     * 생성자
     *
     * @param description 각 카테고리의 한글 설명
     */
    NoticeCategory(String description) {
        this.description = description;
    }

    /**
     * 카테고리 한글 설명을 반환한다.
     *
     * @return 카테고리 설명
     */
    public String getDescription() {
        return description;
    }
}
