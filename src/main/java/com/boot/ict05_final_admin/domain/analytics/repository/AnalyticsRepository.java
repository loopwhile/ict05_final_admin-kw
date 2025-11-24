package com.boot.ict05_final_admin.domain.analytics.repository;

import com.boot.ict05_final_admin.domain.analytics.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 통계(Analytics) 도메인의 QueryDSL 기반 데이터 접근 레이어 인터페이스.
 *
 * <p>
 * KPI, 주문, 재료, 시간·요일 분석과 관련된 모든 조회 메서드를 정의한다.
 * 실제 구현체({@code AnalyticsRepositoryImpl})에서 QueryDSL을 이용하여
 * DTO 프로젝션 기반으로 성능 최적화된 쿼리를 수행한다.
 * </p>
 *
 * <h3>설계 원칙</h3>
 * <ul>
 *   <li>모든 조회는 DTO Projection으로 반환 (엔티티 직접 조회 금지)</li>
 *   <li>조회형 메서드에는 {@code @Transactional(readOnly = true)} 적용</li>
 *   <li>카드 요약형 메서드는 {@code findXxxSummary()} 패턴으로 통일</li>
 *   <li>테이블형 메서드는 {@code findXxx(cond, pageable)} 패턴으로 통일</li>
 *   <li>총 레코드 수는 {@code countXxx(cond)} 로 별도 제공</li>
 * </ul>
 *
 * @author ICT
 * @since 2025.10
 */
public interface AnalyticsRepository {

    /**
     * 가맹점 선택용 옵션 리스트를 조회한다.
     *
     * <p>필터 조건 없이 모든 가맹점의 ID와 이름을 반환한다.</p>
     *
     * @return 가맹점 옵션 DTO 리스트
     */
    List<StoreOptionDto> findStoreOptions();

    // ===================== KPI =====================

    /**
     * KPI 카드(요약) 데이터를 조회한다.
     *
     * @return KPI 카드 요약 DTO
     */
    KpiCardsDto findKpiSummary();

    /**
     * KPI 테이블 데이터를 페이지 단위로 조회한다.
     *
     * @param cond     조회 조건 (기간, 가맹점 등)
     * @param pageable 페이지 정보
     * @return KPI 행 DTO 페이지
     */
    Page<KpiRowDto> findKpi(AnalyticsSearchDto cond, Pageable pageable);

    /**
     * KPI 조회 결과의 총 행 수를 반환한다.
     *
     * @param cond 조회 조건
     * @return 총 행 수
     */
    long countKpi(AnalyticsSearchDto cond);

    // ===================== Orders =====================

    /**
     * 주문 카드(요약) 데이터를 조회한다.
     *
     * @return 주문 카드 요약 DTO
     */
    OrdersCardsDto findOrdersSummary();

    /**
     * 주문 테이블 데이터를 페이지 단위로 조회한다.
     *
     * @param cond     조회 조건 (기간, 가맹점, 주문유형 등)
     * @param pageable 페이지 정보
     * @return 주문 행 DTO 페이지
     */
    Page<OrdersRowDto> findOrders(AnalyticsSearchDto cond, Pageable pageable);

    /**
     * 주문 조회 결과의 총 행 수를 반환한다.
     *
     * @param cond 조회 조건
     * @return 총 행 수
     */
    long countOrders(AnalyticsSearchDto cond);

    // ===================== Materials =====================

    /**
     * 재료 카드(요약) 데이터를 조회한다.
     *
     * @return 재료 카드 요약 DTO
     */
    MaterialsCardsDto findMaterialsSummary();

    /**
     * 재료 테이블 데이터를 페이지 단위로 조회한다.
     *
     * @param cond     조회 조건 (기간, 가맹점, 재료 등)
     * @param pageable 페이지 정보
     * @return 재료 행 DTO 페이지
     */
    Page<MaterialsRowDto> findMaterials(AnalyticsSearchDto cond, Pageable pageable);

	/** 재료 조회 결과의 총 행 수를 반환한다. */
	long countMaterials(AnalyticsSearchDto cond);

    // ===================== Time Analysis =====================

    /**
     * 시간·요일 분석 카드(요약) 데이터를 조회한다.
     *
     * @return 시간·요일 카드 요약 DTO
     */
    TimeChartCardDto findTimeChartSummary();

    /**
     * 시간·요일 분석 차트 데이터를 조회한다.
     *
     * @param cond 조회 조건 (기간, 일/월 모드 등)
     * @return 시간·요일 차트 DTO
     */
    TimeChartRowDto findTimeChart(AnalyticsSearchDto cond);

    /**
     * 시간·요일 분석 테이블 데이터를 페이지 단위로 조회한다.
     *
     * @param cond     조회 조건 (기간, 일/월 모드 등)
     * @param pageable 페이지 정보
     * @return 시간·요일 행 DTO 페이지
     */
    Page<TimeRowDto> findTimeRows(AnalyticsSearchDto cond, Pageable pageable);

    /**
     * 시간·요일 분석 결과의 총 행 수를 반환한다.
     *
     * @param cond 조회 조건
     * @return 총 행 수
     */
    long countTime(AnalyticsSearchDto cond);
}
