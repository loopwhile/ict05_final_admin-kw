package com.boot.ict05_final_admin.domain.notice.entity;

/**
 * 공지사항 상태 Enum
 *
 * <p>공지사항의 상태를 정의하며, 각 항목은 한글 설명(description)을 가진다.</p>
 *
 * <p>주요 상태:</p>
 * <ul>
 *     <li>ACTIVE: 활성</li>
 *     <li>INACTIVE: 비활성</li>
 *     <li>DELETED: 삭제</li>
 * </ul>
 */
public enum NoticeStatus {

    /** 활성 상태 */
    ACTIVE("활성"),

    /** 비활성 상태 */
    INACTIVE("비활성"),

    /** 삭제 상태 */
    DELETED("삭제");

    /** 한글 설명(= DB ENUM 저장값) */
    private final String description;

    /**
     * 생성자
     *
     * @param description 각 상태의 한글 설명
     */
    NoticeStatus(String description) {
        this.description = description;
    }

    /**
     * 상태의 한글 설명을 반환한다.
     *
     * @return 상태 설명
     */
    public String getDescription() {
        return description;
    }
}