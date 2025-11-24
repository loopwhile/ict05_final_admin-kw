package com.boot.ict05_final_admin.domain.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * v_inventory_log 뷰 매핑 엔티티.
 *
 * <p>
 * 읽기 전용(View) 매핑으로, 입고/출고/조정 로그를 통합 조회한다.
 * JPA 영속성 컨텍스트를 통해 INSERT/UPDATE 수행 금지(스키마도 불허).
 * </p>
 *
 * <p>필드 규칙:</p>
 * <ul>
 *   <li>{@code quantity}, {@code stockAfter}, {@code unitPrice}: DECIMAL(15,3) 스케일 가정(뷰 정의에 따름)</li>
 *   <li>{@code date}: ISO-8601 LocalDateTime</li>
 *   <li>{@code type}: INCOME / OUTGO / ADJUST</li>
 *   <li>{@code batchId}: 보통 입고(INCOME)에서만 세팅되며, 정책에 따라 조정(ADJUST)에서도 세팅될 수 있음</li>
 * </ul>
 */
@Entity
@Table(name = "v_inventory_log")
@Immutable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryLogView {

    /** 뷰 전용 PK(ROW_NUMBER 등으로 생성된 고유 값) */
    @Id
    @Column(name = "row_id")
    private Long rowId;

    /** 업무용 로그 ID(입고/출고/조정 원본 PK) */
    @Column(name = "log_id")
    private Long logId;

    /** 로그 일시 */
    @Column(name = "log_date")
    private LocalDateTime date;

    /** 재료 ID */
    @Column(name = "material_id")
    private Long materialId;

    /** 메모 */
    @Column(name = "memo")
    private String memo;

    /** 변동 수량(+입고, -출고, ±조정) */
    @Column(name = "quantity")
    private BigDecimal quantity;

    /** 로그 반영 후 재고 */
    @Column(name = "stock_after")
    private BigDecimal stockAfter;

    /** 가맹점 ID(null 가능) */
    @Column(name = "store_id")
    private Long storeId;

    /** 로그 유형: INCOME / OUTGO / ADJUST */
    @Column(name = "log_type")
    private String type;

    /** 단가(null 가능) — 입고 단가 또는 출고 단가 */
    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    /** LOT 상세용 배치 PK(주로 입고에서 세팅) */
    @Column(name = "batch_id")
    private Long batchId;

    /** 가맹점명(null 가능) */
    @Column(name = "store_name")
    private String storeName;
}
