package com.boot.ict05_final_admin.domain.store.entity;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 가맹점 운영 상태 Enum.
 *
 * <p>
 * JPA에서 {@code @Enumerated(EnumType.STRING)} 으로 저장되며,<br>
 * DB는 영문 상수명(OPERATING)으로 저장하고,<br>
 * 화면에서는 한국어 라벨({@link #description})로 표시할 수 있다.
 * </p>
 */
@Schema(description = "가맹점 운영 상태 Enum")
public enum StoreStatus {

    /** 운영 중 */
    @Schema(description = "운영 중인 매장")
    OPERATING("운영"),

    /** 개점 준비 */
    @Schema(description = "개점 준비 중인 매장")
    PREPARING("개점준비"),

    /** 폐업 상태 */
    @Schema(description = "폐업한 매장")
    CLOSED("폐업");

    /** 한글 라벨 */
    private final String description;

    StoreStatus(String description) {
        this.description = description;
    }

    /** 한국어 라벨 반환 */
    public String getDescription() {
        return description;
    }
}
