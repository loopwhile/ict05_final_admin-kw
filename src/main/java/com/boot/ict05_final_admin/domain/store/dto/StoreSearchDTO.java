package com.boot.ict05_final_admin.domain.store.dto;

import com.boot.ict05_final_admin.domain.store.entity.StoreStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 매장 목록 검색 조건 DTO.
 *
 * <p>
 * 목록 조회 시 전달되는 가벼운 파라미터 컨테이너로,<br>
 * 검색어, 검색 유형, 페이지 크기, 상태 필터를 담는다.
 * </p>
 */
@Data
@Schema(description = "매장 목록 검색 조건 DTO")
public class StoreSearchDTO {

    /** 검색어(매장명, 점주명 등) */
    @Schema(description = "검색어(매장명/점주명 등)", example = "강남")
    private String keyword;        // 검색어(매장명, 점주명 등).

    /** 검색 유형 키 (예: name, owner 등) */
    @Schema(description = "검색 유형 키(예: name, owner 등)", example = "name")
    private String type;           // 검색 유형 키.

    /** 페이지 크기(문자열, 기본 10) */
    @Schema(description = "페이지 크기(문자열)", example = "10", defaultValue = "10")
    private String size = "10";    // 페이지 크기(문자열).

    /** 가맹점 상태 필터 (USE / STOP 등) */
    @Schema(description = "가맹점 상태 필터", example = "ACTIVE")
    private StoreStatus status;
}
