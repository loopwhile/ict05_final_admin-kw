package com.boot.ict05_final_admin.domain.store.entity;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 가맹점 구분 Enum.
 *
 * <p>
 * JPA에서 {@code @Enumerated(EnumType.STRING)}로 저장되며,<br>
 * DB에는 영문 상수(DIRECT/FRANCHISE)로 저장된다.
 * </p>
 */
@Schema(description = "가맹점 구분 Enum")
public enum StoreType {

    /** 직영점 */
    @Schema(description = "본사가 직접 운영하는 직영점")
    DIRECT("직영점"),

    /** 가맹점 */
    @Schema(description = "가맹 계약된 가맹점")
    FRANCHISE("가맹점");

    /** 한글 라벨 */
    private final String description;

    StoreType(String description) {
        this.description = description;
    }

    /** 한국어 라벨 반환 */
    public String getDescription() {
        return description;
    }
}
