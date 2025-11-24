package com.boot.ict05_final_admin.domain.inventory.dto;

import com.boot.ict05_final_admin.domain.inventory.entity.MaterialStatus;
import lombok.Data;

/**
 * 재료 검색 DTO.
 *
 * <p>재료 목록 화면/엑셀 다운로드에 공통으로 사용하는 검색 파라미터 컨테이너.</p>
 *
 * <p>필드 규칙:</p>
 * <ul>
 *   <li>{@code s}: 자유 검색어(재료명/코드 등 백엔드 구현에 따라 매핑)</li>
 *   <li>{@code type}: 검색 대상 필드/모드 식별자(예: "name", "code"). 미사용 시 null/빈값</li>
 *   <li>{@code size}: 페이지 크기 문자열(기본 "10"). 컨트롤러에서 정수로 파싱해 사용</li>
 *   <li>{@code status}: 재료 상태 필터(USE/STOP), 선택</li>
 * </ul>
 */
@Data
public class MaterialSearchDTO {

    /** 자유 검색어(예: 재료명/코드) */
    private String s;

    /** 검색 대상 필드/모드 식별자(예: "name", "code") */
    private String type;

    /** 페이지 크기(문자열, 기본 "10") */
    private String size = "10";

    /** 재료 상태 필터(USE / STOP) */
    private MaterialStatus status;
}
