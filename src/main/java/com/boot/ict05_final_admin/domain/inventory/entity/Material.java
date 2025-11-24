package com.boot.ict05_final_admin.domain.inventory.entity;

import com.boot.ict05_final_admin.domain.inventory.dto.MaterialModifyFormDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 재료(Material) 엔티티.
 *
 * <p>본사 재료 마스터를 표현하는 엔티티로, 재료 코드, 명칭, 카테고리, 단위,
 * 공급업체, 보관 온도, 상태, 적정 재고 등 마스터 속성을 관리한다.</p>
 *
 * <p>재료 수정 시 {@link #updateMaterial(MaterialModifyFormDTO)}를 통해
 * 변경 가능 속성을 갱신한다.</p>
 *
 * @author 김주연
 * @since 2025-10-15
 */
@Entity
@Table(name = "material")
@DynamicUpdate
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Material {

    /** 재료 고유 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "material_id", nullable = false, columnDefinition = "BIGINT UNSIGNED")
    @Comment("재료 시퀀스")
    private Long id;

    /** 재료 코드(카테고리 규칙 기반 자동 생성 정책) */
    @Column(name = "material_code", length = 30, nullable = false, unique = true)
    @Comment("재료 코드 - 등록 시 카테고리 규칙 기반 생성")
    private String code;

    /** 재료명 */
    @Column(name = "material_name", length = 100, nullable = false)
    @Comment("재료명")
    private String name;

    /** 재료 카테고리 */
    @Enumerated(EnumType.STRING)
    @Column(name = "material_category", length = 50, nullable = false,
            columnDefinition = "ENUM('BASE','SIDE','SAUCE','TOPPING','BEVERAGE','PACKAGE','ETC')")
    @Comment("재료 카테고리")
    private MaterialCategory materialCategory;

    /** 기본 단위(소진 단위) */
    @Column(name = "material_base_unit", length = 20, nullable = false)
    @Comment("기본 단위(소진 단위)")
    private String baseUnit;

    /** 판매 단위 */
    @Column(name = "material_sales_unit", length = 20, nullable = false)
    @Comment("판매 단위")
    private String salesUnit;

    /** 변환비율(판매단위 → 기본단위) */
    @Column(name = "material_conversion_rate", nullable = false, columnDefinition = "INT DEFAULT 1000")
    @Comment("변환비율(판매단위 → 기본단위)")
    private Integer conversionRate;

    /** 공급업체명 */
    @Column(name = "material_supplier", length = 100)
    @Comment("재료 공급업체명")
    private String supplier;

    /** 재료 보관온도 */
    @Enumerated(EnumType.STRING)
    @Column(name = "material_temperature")
    @Comment("재료 보관온도")
    private MaterialTemperature materialTemperature;

    /** 재료 상태(USE/STOP) */
    @Enumerated(EnumType.STRING)
    @Column(name = "material_status", nullable = false)
    @Comment("재료 상태 (USE/STOP)")
    private MaterialStatus materialStatus;

    /** 등록일시 */
    @CreationTimestamp
    @Column(name = "material_reg_date", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    @Comment("등록일시")
    private LocalDateTime regDate;

    /** 수정일시 */
    @UpdateTimestamp
    @Column(name = "material_modify_date",
            columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    @Comment("수정일시")
    private LocalDateTime modifyDate;

    /**
     * 본사 기준 적정 재고 수량.
     *
     * <p>DECIMAL(15,3) 스케일을 사용하며, 엔티티 기본값은 0이다.
     * 수정 시 {@link MaterialModifyFormDTO#getOptimalQuantity()}가
     * null인 경우 기존 값을 유지한다.</p>
     */
    @Setter
    @Builder.Default
    @Column(name = "material_optimal_quantity", precision = 15, scale = 3,
            columnDefinition = "DECIMAL(15,3) DEFAULT 0")
    @Comment("본사 기준 적정 재고 수량")
    private BigDecimal optimalQuantity = BigDecimal.ZERO;

    /**
     * 재료 정보를 수정한다.
     *
     * <p>전달된 {@link MaterialModifyFormDTO}의 값으로 변경 가능 속성을 갱신한다.
     * 적정 재고량({@code optimalQuantity})의 경우 DTO 값이 null이면
     * 현재 값을 그대로 유지하고, null이 아니면 해당 값으로 덮어쓴다.</p>
     *
     * @param dto 수정 데이터 DTO
     */
    public void updateMaterial(MaterialModifyFormDTO dto) {
        this.name = dto.getName();
        this.materialCategory = dto.getMaterialCategory();
        this.baseUnit = dto.getBaseUnit();
        this.salesUnit = dto.getSalesUnit();
        this.conversionRate = dto.getConversionRate();
        this.supplier = dto.getSupplier();
        this.materialTemperature = dto.getMaterialTemperature();
        this.materialStatus = dto.getMaterialStatus();
        this.modifyDate = LocalDateTime.now();

        if (dto.getOptimalQuantity() != null) {
            this.optimalQuantity = dto.getOptimalQuantity();
        }
    }
}
