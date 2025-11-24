package com.boot.ict05_final_admin.domain.receiveOrder.dto;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryStatus;
import com.boot.ict05_final_admin.domain.inventory.entity.MaterialCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 수주 상세 내 주문 상품(자재) 항목 DTO
 *
 * <p>수주 상세 화면 또는 API 응답에서 사용되는 단일 주문 품목 데이터를 담는 DTO이다.</p>
 *
 * <p>주요 필드:</p>
 * <ul>
 *     <li><b>name</b> — 주문 상품명</li>
 *     <li><b>materialCategory</b> — 자재 카테고리 ({@link MaterialCategory})</li>
 *     <li><b>detailCount</b> — 주문 수량</li>
 *     <li><b>detailUnitPrice</b> — 단가</li>
 *     <li><b>detailTotalPrice</b> — 총액 (수량 × 단가)</li>
 *     <li><b>inventoryStatus</b> — 재고 상태 ({@link InventoryStatus})</li>
 * </ul>
 *
 * <p>또한 {@link #getInventoryStatusDescription()}과 {@link #getMaterialCategoryDescription()}을 통해
 * Enum 값을 한글 설명으로 변환하여 화면에 표시할 수 있다.</p>
 *
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReceiveOrderItemDTO {

    /** 자재 ID (출고/재고 연동용 FK) */
    private Long materialId;
    
    /** 주문 상품명 */
    public String name;

    /** 주문 상품 카테고리 */
    public MaterialCategory materialCategory;

    /** 주문 상품 수량 */
    public Integer detailCount;

    /** 주문 상품 단가 */
    public BigDecimal detailUnitPrice;

    /** 주문 상품 총액 (수량*단가) */
    public BigDecimal detailTotalPrice;

    /** 주문 상품 재고상태 */
    public InventoryStatus inventoryStatus;

    /**
     * 재고 상태(Enum)의 한글 설명 반환
     *
     * @return 재고 상태 설명 (예: "충분", "부족", "품절") — 상태가 없으면 빈 문자열
     */
    public String getInventoryStatusDescription() {
        return inventoryStatus != null ? inventoryStatus.getDescription() : "";
    }

    /**
     * 자재 카테고리(Enum)의 한글 설명 반환
     *
     * @return 자재 카테고리 설명 (예: "기본재료", "사이드", "소스", "토핑", "음료", "패키지", "기타") — 값이 없으면 빈 문자열
     */
    public String getMaterialCategoryDescription() {
        return  materialCategory != null ? materialCategory.getDescription() : "";
    }

}
