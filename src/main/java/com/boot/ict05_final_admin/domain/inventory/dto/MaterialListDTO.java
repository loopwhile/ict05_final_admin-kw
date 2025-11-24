package com.boot.ict05_final_admin.domain.inventory.dto;

import com.boot.ict05_final_admin.domain.inventory.entity.Material;
import com.boot.ict05_final_admin.domain.inventory.entity.MaterialCategory;
import com.boot.ict05_final_admin.domain.inventory.entity.MaterialStatus;
import com.boot.ict05_final_admin.domain.inventory.entity.MaterialTemperature;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 재료 목록 행 DTO.
 *
 * <p>SSR/JSON 목록(grid) 표시용 요약 정보 컨테이너로,
 * {@link Material} 엔티티를 목록 뷰 모델로 변환해 전달한다.</p>
 *
 * <p>필드 규칙:</p>
 * <ul>
 *   <li>{@code conversionRate}: 판매단위 → 기본단위 변환비율(예: 1 BOX → 20 EA = 20)</li>
 *   <li>{@code materialStatus}: 사용 여부/노출 상태 등 도메인 정책을 따른다</li>
 *   <li>{@code materialTemperature}: 보관 온도 구분(BASE/CHILL/FROZEN 등 프로젝트 정의)</li>
 * </ul>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MaterialListDTO {

    /** 재료 고유 ID */
    private Long id;

    /** 재료 코드 */
    private String code;

    /** 재료명 */
    private String name;

    /** 재료 카테고리 */
    private MaterialCategory materialCategory;

    /** 기본 단위(소진 단위) */
    private String baseUnit;

    /** 판매 단위 */
    private String salesUnit;

    /** 판매단위 → 기본단위 변환비율 (예: BOX→EA 비율) */
    private Integer conversionRate;

    /** 공급업체명 */
    private String supplier;

    /** 보관 온도 구분 */
    private MaterialTemperature materialTemperature;

    /** 재료 상태 */
    private MaterialStatus materialStatus;

    /**
     * 카테고리 한글 설명 반환.
     *
     * @return 카테고리 설명(없으면 빈 문자열)
     */
    public String getCategoryDescription() {
        return materialCategory != null ? materialCategory.getDescription() : "";
    }

    /**
     * 엔티티 기반 생성자.
     *
     * @param material 재료 엔티티
     */
    public MaterialListDTO(Material material) {
        this.id = material.getId();
        this.code = material.getCode();
        this.name = material.getName();
        this.materialCategory = material.getMaterialCategory();
        this.baseUnit = material.getBaseUnit();
        this.salesUnit = material.getSalesUnit();
        this.conversionRate = material.getConversionRate();
        this.supplier = material.getSupplier();
        this.materialTemperature = material.getMaterialTemperature();
        this.materialStatus = material.getMaterialStatus();
    }
}
