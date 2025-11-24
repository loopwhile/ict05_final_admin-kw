package com.boot.ict05_final_admin.domain.inventory.dto;

import com.boot.ict05_final_admin.domain.inventory.entity.MaterialStatus;
import com.boot.ict05_final_admin.domain.inventory.entity.MaterialTemperature;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 가맹점 재료 목록 DTO.
 * <p>목록(SSR/JSON) 화면 조회용 요약 정보 컨테이너.</p>
 *
 * <p>필드 규칙:</p>
 * <ul>
 *   <li>{@code quantity}, {@code optimalQuantity}: 정수 개수(스토어 측 재고 수량 정책에 따름)</li>
 *   <li>{@code purchasePrice}, {@code sellingPrice}: 금액(DECIMAL(15,3) 가정, 통화 단위는 시스템 공통 정책)</li>
 *   <li>{@code expirationDate}: ISO-8601 LocalDate</li>
 *   <li>{@code isHqMaterial}: 본사 재료 매핑 여부(true면 본사 기준으로 생성/매핑된 재료)</li>
 * </ul>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoreMaterialListDTO {

    /** 가맹점 재료 고유 ID */
    private Long id;

    /** 가맹점 재료 코드 */
    private String code;

    /** 가맹점 재료명 */
    private String name;

    /** 카테고리(표시용 문자열) */
    private String category;

    /** 기본 단위(소진 단위, 예: g, ml, EA 등) */
    private String baseUnit;

    /** 판매 단위(예: BOX, PACK 등 — 본사 기준 표시용) */
    private String salesUnit;

    /** 공급업체명 */
    private String supplier;

    /** 보관 온도 구분 */
    private MaterialTemperature temperature;

    /** 재료 상태 */
    private MaterialStatus status;

    /** 현재 수량(정수) */
    private Integer quantity;

    /** 적정 수량(정수) */
    private Integer optimalQuantity;

    /** 매입가(DECIMAL(15,3)) */
    private BigDecimal purchasePrice;

    /** 판매가/공급가(DECIMAL(15,3)) */
    private BigDecimal sellingPrice;

    /** 유통기한(ISO-8601 LocalDate) */
    private LocalDate expirationDate;

    /** 본사 재료 매핑 여부(true=HQ 매핑 재료) */
    private boolean isHqMaterial;
}
