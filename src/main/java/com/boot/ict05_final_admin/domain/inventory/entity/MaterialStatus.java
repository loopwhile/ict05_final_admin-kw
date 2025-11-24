package com.boot.ict05_final_admin.domain.inventory.entity;

/**
 * 재료 상태 Enum.
 *
 * <p>재료의 상태를 정의하며, 각 항목은 한글 설명(description)을 가진다.</p>
 *
 * <p>상태 값:</p>
 * <ul>
 *   <li>USE: 사용 중</li>
 *   <li>STOP: 사용 중단</li>
 * </ul>
 */
public enum MaterialStatus {

    /** 사용 중 */
    USE("사용 중"),

    /** 사용 중단 */
    STOP("사용 중단");

    /** 한글 설명 */
    private final String description;

    /**
     * 생성자.
     *
     * @param description 상태의 한글 설명
     */
    MaterialStatus(String description) {
        this.description = description;
    }

    /**
     * 상태의 한글 설명을 반환한다.
     *
     * @return 한글 설명
     */
    public String getDescription() {
        return description;
    }
}
