package com.boot.ict05_final_admin.domain.receiveOrder.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.QInventory;
import com.boot.ict05_final_admin.domain.inventory.entity.QMaterial;
import com.boot.ict05_final_admin.domain.inventory.entity.QStoreMaterial;
import com.boot.ict05_final_admin.domain.receiveOrder.dto.*;
import com.boot.ict05_final_admin.domain.receiveOrder.entity.*;
import com.boot.ict05_final_admin.domain.store.entity.QStore;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 수주(ReceiveOrder) 도메인의 QueryDSL 전용 레포지토리 구현체.
 *
 * <p>
 * 기본 CRUD 기능은 JpaRepository(ReceiveOrderRepository)가 담당하고,
 * 이 클래스는 화면 요구사항에 맞춘 복합 조회와 상태 변경 쿼리를
 * QueryDSL 기반으로 구현한다.
 * </p>
 *
 * <p>주요 역할</p>
 * <ul>
 *     <li>대시보드 상단 카드용 요약 데이터 조회(getSummary)</li>
 *     <li>검색/필터/페이징이 적용된 수주 목록 조회(listReceive, countReceive)</li>
 *     <li>수주 상세 헤더 및 품목 리스트 조회(findDetailById, findItemsByOrderId)</li>
 *     <li>수주 상태 변경용 벌크 업데이트(updateStatusIfCurrent, updateStatusByOrderCode)</li>
 * </ul>
 *
 * <p>
 * update 계열 메서드는 벌크 쿼리를 사용하므로, 실행 후 EntityManager flush/clear 를 통해
 * 영속성 컨텍스트를 초기화하여 캐시 일관성을 보장한다.
 * </p>
 */
@Repository
@RequiredArgsConstructor
public class ReceiveOrderRepositoryImpl implements ReceiveOrderRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    // 벌크 업데이트 후 영속성 컨텍스트 초기화를 위해 사용하는 {@link EntityManager}.
    @PersistenceContext
    private EntityManager em;

    /**
     * 수주 대시보드 상단 카드에 표시할 요약 데이터를 조회한다.
     *
     * <ul>
     *     <li>총 수주 건수 (금액, 수량이 0 초과인 건만 집계)</li>
     *     <li>배송 중 수주 건수</li>
     *     <li>우선(URGENT) 수주 건수</li>
     *     <li>총 수주 금액 합계</li>
     * </ul>
     *
     * @return 요약 정보를 담은 {@link ReceiveOrderSummaryDTO}
     */
    @Override
    public ReceiveOrderSummaryDTO getSummary() {
        QReceiveOrder ro = QReceiveOrder.receiveOrder;

        Long totalCount = queryFactory
                .select(ro.id.count())
                .from(ro)
                .where(ro.totalPrice.gt(BigDecimal.ZERO)
                        .and(ro.totalCount.gt(0)))
                .fetchOne();

        Long shippingCount = queryFactory
                .select(ro.id.count())
                .from(ro)
                .where(ro.status.eq(ReceiveOrderStatus.SHIPPING))
                .fetchOne();

        Long urgentCount = queryFactory
                .select(ro.id.count())
                .from(ro)
                .where(ro.priority.eq(ReceiveOrderPriority.URGENT))
                .fetchOne();

        BigDecimal totalAmount = queryFactory
                .select(ro.totalPrice.sum().coalesce(BigDecimal.ZERO))
                .from(ro)
                .where(ro.totalPrice.gt(BigDecimal.ZERO))
                .fetchOne();

        return new ReceiveOrderSummaryDTO(
                totalCount != null ? totalCount : 0L,
                totalAmount != null ? totalAmount : BigDecimal.ZERO,
                shippingCount != null ? shippingCount : 0L,
                urgentCount != null ? urgentCount : 0L
        );
    }

    /**
     * 수주 ID 로 단일 수주 엔티티를 조회한다.
     *
     * @param id 수주 ID
     * @return 조회된 {@link ReceiveOrder} 가 존재하면 Optional 로 감싸서 반환하고,
     *         없으면 {@link Optional#empty()} 반환
     */
    @Override
    public Optional<ReceiveOrder> findOrderById(Long id) {
        QReceiveOrder ro = QReceiveOrder.receiveOrder;

        ReceiveOrder result = queryFactory
                .selectFrom(ro)
                .where(ro.id.eq(id))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    /**
     * 현재 상태가 지정된 상태인 경우에만 수주 상태를 다음 상태로 변경한다.
     *
     * <p>
     * 동시성 상황에서 이전에 다른 스레드가 상태를 변경한 경우
     * 영향받는 행이 0 이 되도록 방어한다.
     * </p>
     *
     * @param id   수주 ID
     * @param curr 현재 기대하는 상태
     * @param next 변경할 다음 상태
     * @return 실제로 업데이트된 행 수 (0 또는 1)
     */
    @Override
    public int updateStatusIfCurrent(Long id, ReceiveOrderStatus curr, ReceiveOrderStatus next) {
        QReceiveOrder ro = QReceiveOrder.receiveOrder;

        long updated = queryFactory
                .update(ro)
                .set(ro.status, next)
                .where(
                        ro.id.eq(id)
                                .and(ro.status.eq(curr))
                )
                .execute();

        em.flush();
        em.clear();

        return (int) updated;
    }

    /**
     * 주문 번호를 기준으로 수주 상태를 일괄 변경한다.
     *
     * <p>
     * 하나의 주문 번호에 여러 건이 묶여 있다면 해당 모든 레코드의 상태를 변경한다.
     * </p>
     *
     * @param orderCode 주문 번호
     * @param status    변경할 상태
     * @return 업데이트된 행 수
     */
    @Override
    public int updateStatusByOrderCode(String orderCode, ReceiveOrderStatus status) {
        QReceiveOrder ro = QReceiveOrder.receiveOrder;

        var updateClause = queryFactory
                .update(ro)
                .set(ro.status, status)
                .where(ro.orderCode.eq(orderCode));

        if (status == ReceiveOrderStatus.DELIVERED) {
            updateClause.set(ro.actualDeliveryDate, LocalDate.now());
        }

        long updated = updateClause.execute();

        em.flush();
        em.clear();

        return (int) updated;
    }

    /**
     * 수주 목록을 페이지 단위로 조회한다.
     *
     * <p>
     * 검색 조건:
     * </p>
     * <ul>
     *     <li>상태(receiveOrderStatus)</li>
     *     <li>검색어 타입(type: all, orderCode, storeName, storeLocation)</li>
     *     <li>검색어(s)</li>
     * </ul>
     *
     * <p>
     * 추가로 수주 상세가 하나라도 존재하는 주문만 대상으로 한다.
     * 품목 수는 해당 수주의 {@code ReceiveOrderDetail.count} 합계 기준이다.
     * </p>
     *
     * @param receiveOrderSearchDTO 검색 조건 DTO
     * @param pageable              페이징 정보
     * @return 수주 목록 페이지
     */
    @Override
    public Page<ReceiveOrderListDTO> listReceive(ReceiveOrderSearchDTO receiveOrderSearchDTO, Pageable pageable) {
        QReceiveOrder ro = QReceiveOrder.receiveOrder;
        QReceiveOrderDetail rod = QReceiveOrderDetail.receiveOrderDetail;
        QStore store = QStore.store;

        // details 존재 여부 → exists 서브쿼리
        BooleanExpression hasDetails = JPAExpressions
                .selectOne()
                .from(rod)
                .where(rod.receiveOrder.eq(ro))
                .exists();

        // 품목수 서브쿼리: 현재 수주 건의 detail 수량 합
        //   - 품목 라인 수로 쓰고 싶으면 rod.id.count()
        //   - 주문 총 개수로 쓰고 싶으면 rod.count.sum()
        var totalItemCountExpr = ExpressionUtils.as(
                JPAExpressions
                        .select(rod.count.sum().coalesce(0))     // null 이면 0으로
                        .from(rod)
                        .where(rod.receiveOrder.eq(ro)),
                "totalCount"
        );

        // 데이터 목록 조회
        List<ReceiveOrderListDTO> content = queryFactory
                .select(Projections.fields(ReceiveOrderListDTO.class,
                        ro.id,
                        ro.orderCode,
                        ro.store.name.as("storeName"),
                        ro.store.location.as("storeLocation"),
                        ro.status,
                        ro.priority,
                        ro.totalPrice,
                        totalItemCountExpr,
                        ro.actualDeliveryDate
                ))
                .from(ro)
                .join(ro.store, store)
                .where(
                        eqOrderCode(receiveOrderSearchDTO, ro),
                        hasDetails                          // 주문 건이 있는 주문만
                )
                .orderBy(ro.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 카운트 조회
        long total = queryFactory
                .select(ro.count())
                .from(ro)
                .where(
                        eqOrderCode(receiveOrderSearchDTO, ro),
                        hasDetails
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * 수주 검색용 동적 조건을 생성한다.
     *
     * <p>
     * 기본적으로 항상 참인 조건에서 시작해서 아래 조건을 조합한다.
     * </p>
     *
     * <ul>
     *     <li>상태 필터: {@link ReceiveOrderSearchDTO#getReceiveOrderStatus()}</li>
     *     <li>검색어 타입: type (all, orderCode, storeName, storeLocation)</li>
     *     <li>검색어: s</li>
     * </ul>
     *
     * @param dto 검색 조건 DTO
     * @param ro  수주 QueryDSL Q타입
     * @return QueryDSL {@link BooleanExpression} 조건
     */
    private BooleanExpression eqOrderCode(ReceiveOrderSearchDTO dto, QReceiveOrder ro) {

        // BooleanExpression condition = null;
        // 기본값 true
        BooleanExpression condition = Expressions.asBoolean(true).isTrue();

        // 상태 필터
        if (dto.getReceiveOrderStatus() != null) {
            condition = condition.and(ro.status.eq(dto.getReceiveOrderStatus()));
        }

        String type = dto.getType();
        String keyword = dto.getS();

        if (keyword == null || keyword.trim().isEmpty()) {
            return condition; // 상태만 필터링
        }

        if (type == null) type = "all";

        switch (type) {
            case "orderCode":
                condition = condition.and(ro.orderCode.containsIgnoreCase(keyword));
                break;
            case "storeName":
                condition = condition.and(ro.store.name.containsIgnoreCase(keyword));
                break;
            case "storeLocation":
                condition = condition.and(ro.store.location.containsIgnoreCase(keyword));
                break;
            case "all":
            default:
                condition = condition.and(
                        ro.orderCode.containsIgnoreCase(keyword)
                                .or(ro.store.name.containsIgnoreCase(keyword))
                                .or(ro.store.location.containsIgnoreCase(keyword))
                );
                break;
        }

        return condition;
    }

    /**
     * 검색 조건에 해당하는 수주 건수(행 수)를 반환한다.
     *
     * <p>
     * 수주 상세가 하나 이상 존재하는 건만 대상으로 하며,
     * 목록 조회와 동일한 필터를 사용한다.
     * </p>
     *
     * @param receiveOrderSearchDTO 검색 조건 DTO
     * @return 전체 수주 건수
     */
    @Override
    public long countReceive(ReceiveOrderSearchDTO receiveOrderSearchDTO) {
        QReceiveOrder ro = QReceiveOrder.receiveOrder;
        QReceiveOrderDetail rod = QReceiveOrderDetail.receiveOrderDetail;

        BooleanExpression hasDetails = JPAExpressions
                .selectOne()
                .from(rod)
                .where(rod.receiveOrder.eq(ro))
                .exists();

        Long total = queryFactory
                .select(ro.count())
                .from(ro)
                .where(
                        eqOrderCode(receiveOrderSearchDTO, ro),
                        hasDetails
                )
                .fetchOne();

        return total != null ? total : 0L;
    }

    /**
     * 수주 ID 기준으로 수주 상세 헤더 정보를 조회한다.
     *
     * <p>
     * 수주 자체 정보와 가맹점 정보, 총 수량, 총 금액, 비고 등을
     * 한 번에 가져와서 팝업 상세 상단 영역에 사용한다.
     * </p>
     *
     * @param id 수주 ID
     * @return 수주 상세 DTO, 없으면 {@link Optional#empty()}
     */
    @Override
    public Optional<ReceiveOrderDetailDTO> findDetailById(Long id) {
        QReceiveOrder ro = QReceiveOrder.receiveOrder;
        QStore store = QStore.store;

        ReceiveOrderDetailDTO dto = queryFactory
                .select(Projections.fields(ReceiveOrderDetailDTO.class,
                        ro.id,
                        ro.orderCode,
                        ro.orderDate,
                        ro.actualDeliveryDate,
                        ro.status,
                        ro.priority,
                        store.name.as("storeName"),
                        store.id.as("storeId"),
                        store.location.as("storeLocation"),
                        ro.totalCount.as("totalCount"),
                        ro.totalPrice,
                        ro.remark
                ))
                .from(ro)
                .leftJoin(ro.store, store)
                .where(ro.id.eq(id))
                .fetchOne();

        return Optional.ofNullable(dto);
    }

    /**
     * 수주 ID 기준으로 수주 상세 품목 리스트를 조회한다.
     *
     * <p>
     * 가맹점 발주 상세(store_material)와 본사 재료(material)를 조인하여
     * 아래 정보를 한 번에 가져온다.
     * </p>
     *
     * <ul>
     *     <li>HQ 재료 ID (materialId)</li>
     *     <li>재료명</li>
     *     <li>재료 카테고리</li>
     *     <li>주문 수량, 단가, 총액</li>
     *     <li>본사 재고 상태 (inventoryStatus, 없을 수 있음)</li>
     * </ul>
     *
     * @param id 수주 ID
     * @return 품목 리스트
     */
    @Override
    public List<ReceiveOrderItemDTO> findItemsByOrderId(Long id) {
        QReceiveOrderDetail rod = QReceiveOrderDetail.receiveOrderDetail;
        QStoreMaterial sm = QStoreMaterial.storeMaterial;
        QMaterial material = QMaterial.material;
        QInventory hq = QInventory.inventory;

        return queryFactory
                .selectDistinct(Projections.fields(ReceiveOrderItemDTO.class,
                        material.id.as("materialId"),
                        material.name.as("name"),
                        material.materialCategory.as("materialCategory"),
                        rod.count.as("detailCount"),
                        rod.unitPrice.as("detailUnitPrice"),
                        rod.totalPrice.as("detailTotalPrice"),
                        hq.status.as("inventoryStatus")
                ))
                .from(rod)
                .join(rod.storeMaterial, sm)       // purchase_order_detail.material_id_fk → store_material
                .leftJoin(sm.material, material)   // store_material.material_id_fk → HQ material (nullable)
                .leftJoin(rod.inventory, hq)            // HQ 재고 (nullable)
                .where(rod.receiveOrder.id.eq(id))
                .fetch();
    }



}

