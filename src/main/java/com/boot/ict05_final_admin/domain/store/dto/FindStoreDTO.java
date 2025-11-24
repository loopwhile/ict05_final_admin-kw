package com.boot.ict05_final_admin.domain.store.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 가맹점 정보 검색 DTO.
 *
 * <p>
 * 매장 검색 또는 특정 매장 정보 조회를 위해 전달되는 가벼운 DTO이다.<br>
 * 매장 ID와 매장명을 포함한다.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "가맹점 정보 조회용 DTO")
public class FindStoreDTO {

    /** 매장 고유 Id */
    @Schema(description = "매장 ID", example = "15")
    private Long storeId;

    /** 가맹점명 */
    @Schema(description = "가맹점명", example = "코딩카페 홍대점")
    private String storeName;
}
