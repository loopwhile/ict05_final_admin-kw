package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.dto.InventoryListDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.InventorySearchDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

/**
 * 본사 재고(Inventory) 조회용 커스텀 리포지토리.
 *
 * <p>재고 목록 화면 그리드에 필요한 DTO 기반 조회 및
 * 검색 조건별 카운트/일괄 업데이트 기능을 정의한다.</p>
 */
public interface InventoryRepositoryCustom {

    /**
     * 본사 재고 목록 페이지 조회.
     *
     * <p>{@link InventorySearchDTO}의 검색 조건(재료명/코드/카테고리/상태 등)과
     * {@link Pageable}의 페이징·정렬 정보를 이용해
     * 화면에 바인딩할 {@link InventoryListDTO} 페이지를 반환한다.</p>
     *
     * @param searchDTO 검색 조건 DTO
     * @param pageable  페이징/정렬 정보
     * @return 재고 목록 DTO 페이지
     */
    Page<InventoryListDTO> listInventory(InventorySearchDTO searchDTO, Pageable pageable);

    /**
     * 본사 재고 목록 총 건수 조회.
     *
     * <p>{@link #listInventory(InventorySearchDTO, Pageable)}와 동일한
     * 검색 조건을 기준으로 총 건수를 반환한다.
     * 페이징 계산 및 UI 표시용으로 사용한다.</p>
     *
     * @param searchDTO 검색 조건 DTO
     * @return 검색 조건에 매칭되는 재고 행 총 건수
     */
    long countInventory(InventorySearchDTO searchDTO);

    /**
     * 단일 재료(materialId)에 대한 인벤토리의 적정 재고를 일괄 갱신한다.
     *
     * <p>본사 재료 마스터의 적정 재고량(material_optimal_quantity)이 변경된 경우,
     * 해당 재료를 참조하는 인벤토리(inventory_optimal_quantity)를
     * 새 값으로 동기화할 때 사용한다.</p>
     *
     * @param materialId    대상 재료 ID
     * @param newOptimalQty 재설정할 적정 재고(DECIMAL(15,3) 가정)
     * @return 업데이트된 인벤토리 행 수
     */
    long updateOptimalQuantityByMaterialId(Long materialId, BigDecimal newOptimalQty);
}
