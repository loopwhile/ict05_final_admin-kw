package com.boot.ict05_final_admin.domain.inventory.dto;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryStatus;
import lombok.Data;

/**
 * 가맹점 재고 검색 DTO.
 *
 * <p>가맹점 재고 목록 페이지 및 REST API에 공통으로 사용하는 검색 파라미터 컨테이너.</p>
 *
 * <p>필드 규칙:</p>
 * <ul>
 *   <li>{@code s}: 자유 검색어(재료명/코드 등 백엔드 구현에 따라 매핑)</li>
 *   <li>{@code type}: 검색 대상 필드/모드 식별자(예: "name", "code"), 선택</li>
 *   <li>{@code size}: 페이지 크기 문자열(기본 "10") — 컨트롤러에서 정수로 파싱해 사용</li>
 *   <li>{@code storeId}: 대상 가맹점 ID(선택). 미지정 시 전체</li>
 *   <li>{@code status}: 재고 상태 필터(SUFFICIENT/LOW/SHORTAGE), 선택</li>
 * </ul>
 */
@Data
public class StoreInventorySearchDTO {

    /** 자유 검색어(예: 재료명/코드) */
    private String s;

    /** 검색 대상 필드/모드 식별자(예: "name", "code") */
    private String type;

    /** 페이지 크기(문자열, 기본 "10") */
    private String size = "10";

    /** 가맹점 ID(선택) */
    private Long storeId;

    /** 재고 상태 필터(SUFFICIENT / LOW / SHORTAGE) */
    private InventoryStatus status;
}
