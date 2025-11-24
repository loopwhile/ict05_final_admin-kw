package com.boot.ict05_final_admin.domain.analytics.repository;

import com.boot.ict05_final_admin.domain.analytics.dto.*;
import com.boot.ict05_final_admin.domain.inventory.entity.*;
import com.boot.ict05_final_admin.domain.order.entity.OrderStatus;
import com.boot.ict05_final_admin.domain.order.entity.OrderType;
import com.boot.ict05_final_admin.domain.order.entity.QCustomerOrder;
import com.boot.ict05_final_admin.domain.order.entity.QCustomerOrderDetail;
import com.boot.ict05_final_admin.domain.receiveOrder.entity.QReceiveOrderDetail;
import com.boot.ict05_final_admin.domain.store.entity.QStore;
import com.boot.ict05_final_admin.domain.menu.entity.QMenu;
import com.boot.ict05_final_admin.domain.menu.entity.QMenuCategory;
import com.boot.ict05_final_admin.domain.receiveOrder.entity.QReceiveOrder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * <h2>AnalyticsRepositoryImpl</h2>
 *
 * <p>
 * 본 클래스는 HQ 관리자용 통계/리포트 도메인에서 사용하는
 * {@link AnalyticsRepository} 인터페이스의 QueryDSL 구현체입니다.
 * </p>
 *
 * <h3>주요 기능</h3>
 * <ul>
 *   <li><b>KPI 분석</b> – 가맹점별·일/월별 매출 및 핵심지표(UPT, ADS, AUR, Comp)</li>
 *   <li><b>주문 분석</b> – 주문별·카테고리별·메뉴별 매출 및 채널별 비율</li>
 *   <li><b>재료 분석</b> – 재료 사용/발주/마진율 통계 (개발 예정)</li>
 *   <li><b>시간 분석</b> – 시간대별·요일별 매출 및 주문 흐름</li>
 * </ul>
 *
 * <h3>Query 설계 원칙</h3>
 * <ul>
 *   <li>모든 쿼리는 <b>QueryDSL 기반 DTO Projection</b> 사용 – 엔티티 직접 조회 금지</li>
 *   <li>WHERE 절은 항상 <b>[start 00:00, end+1 00:00)</b> 구간으로 설정하여 인덱스 사용 극대화</li>
 *   <li><b>함수 기반 조건/그룹핑 금지</b> – YEAR(), MONTH(), DATE() 등은 SELECT 전용으로만 사용</li>
 *   <li>GROUP BY / ORDER BY 절에서 컬럼 함수 사용을 자제하고, 원본 컬럼 그대로 활용</li>
 *   <li>집계는 <b>SUM(CASE WHEN … THEN … ELSE 0 END)</b> 형태로 통합 처리</li>
 *   <li>파생지표(UPT, ADS, AUR, Comp 등)는 DB가 아닌 Java 단에서 계산</li>
 *   <li><b>SELECT DISTINCT 최소화</b> – 풀스캔 유발 방지를 위해 COUNT(DISTINCT …)만 제한적으로 사용</li>
 *   <li><b>JOIN 최소화</b> – 반드시 필요한 관계만 명시적 join(on)으로 수행, 묵시적 조인 금지</li>
 *   <li>트랜잭션은 모두 <b>@Transactional(readOnly=true)</b>로 처리, 변경감지 및 쓰기 지연 비활성화</li>
 *   <li>Hibernate 쿼리 힌트: {@code readOnly=true}, {@code flushMode=COMMIT}, {@code timeout=30000}</li>
 *   <li>대규모 집계 쿼리에서는 <b>ORDER BY NULL</b> 패턴을 사용해 filesort 제거</li>
 *   <li>100건 이하의 소규모 결과는 DB 정렬 제거 후 애플리케이션 측 정렬 수행</li>
 *   <li>EXPLAIN 결과는 type=range/ref, Using index 유지가 기본 목표</li>
 * </ul>
 *
 * <h3>적용 범위</h3>
 * <ul>
 *   <li>KPI 분석 페이지 ({@code /admin/analytics/kpi})</li>
 *   <li>주문 분석 페이지 ({@code /admin/analytics/orders})</li>
 *   <li>재료 분석 페이지 ({@code /admin/analytics/materials})</li>
 *   <li>시간 분석 페이지 ({@code /admin/analytics/time})</li>
 * </ul>
 *
 * @author
 *   ICT 파이널 프로젝트 개발팀
 * @see com.boot.ict05_final_admin.domain.analytics.dto
 * @see com.boot.ict05_final_admin.domain.order.entity.QCustomerOrder
 * @see com.boot.ict05_final_admin.domain.store.entity.QStore
 * @since 2025-11
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class AnalyticsRepositoryImpl implements AnalyticsRepository {

    private final JPAQueryFactory query;

    // Q-classes
    private final QCustomerOrder co = QCustomerOrder.customerOrder;
    private final QCustomerOrderDetail cod = QCustomerOrderDetail.customerOrderDetail;
    private final QStore s = QStore.store;
    private final QMenu m = QMenu.menu;
    private final QMenuCategory mc = QMenuCategory.menuCategory;
    private final QStoreInventory si = QStoreInventory.storeInventory;
    private final QStoreMaterial sm = QStoreMaterial.storeMaterial;
    private final QMaterial mat = QMaterial.material;
    private final QReceiveOrder ro = QReceiveOrder.receiveOrder;
    private final QReceiveOrderDetail rod = QReceiveOrderDetail.receiveOrderDetail;
	private final QInventory inv = QInventory.inventory;
	private final QInventoryOut io = QInventoryOut.inventoryOut;
	private final QUnitPrice up = QUnitPrice.unitPrice;

    private static final ZoneId ZONE_SEOUL = ZoneId.of("Asia/Seoul");

    /**
     * 전체 가맹점 목록을 KPI/주문/시간 분석 페이지의 필터용 옵션으로 조회한다.
     * <p>
     * 본 메서드는 단순히 {@link QStore} 엔티티에서 점포 ID와 이름만 선택하여
     * {@link StoreOptionDto} 리스트로 반환한다.
     * </p>
     *
     * <h3>쿼리 특성</h3>
     * <ul>
     *   <li>조회 대상: 모든 가맹점(store 테이블)</li>
     *   <li>SELECT: id, name</li>
     *   <li>ORDER BY: 점포명 오름차순</li>
     *   <li>Hint: readOnly, flushMode=COMMIT, timeout=30초</li>
     * </ul>
     *
     * @return 가맹점 선택 드롭다운에 표시할 {@link StoreOptionDto} 리스트
     */
    @Override
    @Transactional(readOnly = true)
    public List<StoreOptionDto> findStoreOptions() {
        return readHints(
                query.select(Projections.constructor(StoreOptionDto.class, s.id, s.name))
                        .from(s)
                        .orderBy(s.name.asc())
        ).fetch();
    }

    /**
     * KPI 분석 상단의 요약 카드(8개 항목)를 조회한다.
     *
     * <p>
     * 기준일(오늘)을 기준으로 <b>1월 1일 ~ 어제까지</b>의 누적 매출(YTD)과
     * 전월 대비(MoM), 전년 대비(YoY)를 한 번의 쿼리로 계산한다.
     * 이 메서드는 또한 채널별(방문/포장/배달) 매출 합계와 트랜잭션(주문건수)을 함께 수신하여
     * 프론트에서 카드 형태로 바로 표시할 수 있도록 {@link KpiCardsDto}로 반환한다.
     * </p>
     *
     * <h3>계산 항목</h3>
     * <ul>
     *   <li><b>Sales</b> – 올해 1/1 ~ 어제 매출 합계 (YTD)</li>
     *   <li><b>Transaction</b> – 동일 기간 주문 건수</li>
     *   <li><b>UPT</b> – 총 판매수량 ÷ 주문건수 (Java에서 파생 계산)</li>
     *   <li><b>ADS</b> – 매출 ÷ 주문건수 (Java에서 파생 계산)</li>
     *   <li><b>AUR</b> – 매출 ÷ 판매수량 (Java에서 파생 계산)</li>
     *   <li><b>Comp(MoM)</b> – 이번달(MTD: 이번 달 1일 ~ 어제) vs 전월(PMT 또는 전전월 전체, 윈도우 규칙 적용) 매출 비교</li>
     *   <li><b>Comp(YoY)</b> – 올해(YTD) vs 작년(YTD-1) 매출 비교</li>
     *   <li><b>Order Type Ratio</b> – 방문/포장/배달 매출 비중(%)</li>
     * </ul>
     *
     * <h3>쿼리/성능 노트</h3>
     * <ul>
     *   <li>기간·상태 조건은 넓은 스캔 범위(예: LYTD ~ YTD)를 한 번에 걸어두고, SUM(CASE)로 필요한 기간별 합계를 동시에 추출</li>
     *   <li>수량 합계는 별도 쿼리에서 {@code customer_order_detail}을 집계하여 N+1 및 대형 조인 비용을 회피</li>
     *   <li>모든 조회에 대해 {@link #readHints(JPAQuery)}를 적용하여 readOnly/flushMode/timeout 힌트를 설정</li>
     * </ul>
     *
     * @return KPI 요약 카드 데이터를 담은 {@link KpiCardsDto}
     */
    @Override
    @Transactional(readOnly = true)
    public KpiCardsDto findKpiSummary() {

        /* ---------------------------------------------------------
         * 0) 공통 기간 계산(Asia/Seoul 기준)
         *    - YTD: 당해 1/1 ~ 어제
         *    - MoM 윈도우: (aStart~aEnd) vs (bStart~bEnd)
         *    - LYTD: 전년 동기간(YTD 기준 -1y)
         * --------------------------------------------------------- */
        final var today    = LocalDate.now(ZONE_SEOUL);
        final var ytdStart = LocalDate.of(today.getYear(), 1, 1);
        final var ytdEnd   = today.minusDays(1);

        final MoMWindows w = calcMoMWindows(today);
        final var aStart = w.aStart();
        final var aEnd   = w.aEnd();
        final var bStart = w.bStart();
        final var bEnd   = w.bEnd();

        final var lytdStart = ytdStart.minusYears(1);
        final var lytdEnd   = ytdEnd.minusYears(1);

        final BooleanExpression done = co.status.eq(OrderStatus.COMPLETED);

        /* ---------------------------------------------------------
         * 1) 단일 스캔(co)로 핵심 합계들을 한 번에 계산
         *    - WHERE: [lytdStart, ytdEnd)로 범위를 넉넉히 잡은 뒤
         *      sumIf/countIf 분기로 각 윈도우(YTD, A/B, LYTD, 채널별 YTD)를 동시 계산
         *    - 정렬 불필요 → ORDER BY NULL
         * --------------------------------------------------------- */
        final Tuple t = readHints(
                query.select(
                                // YTD
                                sumIf(betweenDateClosedOpen(co.orderedAt, ytdStart, ytdEnd), co.totalPrice),
                                countIf(betweenDateClosedOpen(co.orderedAt, ytdStart, ytdEnd)),

                                // MoM: A vs B
                                sumIf(betweenDateClosedOpen(co.orderedAt, aStart, aEnd), co.totalPrice),
                                sumIf(betweenDateClosedOpen(co.orderedAt, bStart, bEnd), co.totalPrice),

                                // LYTD
                                sumIf(betweenDateClosedOpen(co.orderedAt, lytdStart, lytdEnd), co.totalPrice),

                                // 채널별 YTD
                                sumIf(betweenDateClosedOpen(co.orderedAt, ytdStart, ytdEnd).and(co.orderType.eq(OrderType.VISIT)),    co.totalPrice),
                                sumIf(betweenDateClosedOpen(co.orderedAt, ytdStart, ytdEnd).and(co.orderType.eq(OrderType.TAKEOUT)),  co.totalPrice),
                                sumIf(betweenDateClosedOpen(co.orderedAt, ytdStart, ytdEnd).and(co.orderType.eq(OrderType.DELIVERY)), co.totalPrice)
                        )
                        .from(co)
                        // lytdStart ~ ytdEnd 전체 구간만 스캔하고, 나머지는 sumIf 분기로 분리
                        .where(done, betweenDateClosedOpen(co.orderedAt, lytdStart, ytdEnd))
                        .orderBy(orderByNull())
        ).fetchOne();

        // 1-1) 널/스케일 안정화
        final BigDecimal ytdSales   = t != null ? nz(t.get(0, BigDecimal.class)) : BigDecimal.ZERO;
        final long       ytdTrx     = t != null ? nz(t.get(1, Long.class))       : 0L;
        final BigDecimal aSales     = t != null ? nz(t.get(2, BigDecimal.class)) : BigDecimal.ZERO;
        final BigDecimal bSales     = t != null ? nz(t.get(3, BigDecimal.class)) : BigDecimal.ZERO;
        final BigDecimal lytdSales  = t != null ? nz(t.get(4, BigDecimal.class)) : BigDecimal.ZERO;
        final BigDecimal visitSales = t != null ? nz(t.get(5, BigDecimal.class)) : BigDecimal.ZERO;
        final BigDecimal takeSales  = t != null ? nz(t.get(6, BigDecimal.class)) : BigDecimal.ZERO;
        final BigDecimal delvSales  = t != null ? nz(t.get(7, BigDecimal.class)) : BigDecimal.ZERO;

        /* ---------------------------------------------------------
         * 2) YTD 판매수량(units)
         *    - cod 조인이 필요한 별도 1쿼리
         *    - 목적: co 단일 스캔에 cod 조인을 섞지 않아 카디널리티 급증 방지
         * --------------------------------------------------------- */
        final Long ytdUnitsL = Optional.ofNullable(
                readHints(
                        query.select(cod.quantity.sum().longValue())
                                .from(co)
                                .join(cod).on(cod.order.eq(co)) // 명시적 조인
                                .where(done, betweenDateClosedOpen(co.orderedAt, ytdStart, ytdEnd))
                                .orderBy(orderByNull())
                ).fetchOne()
        ).orElse(0L);

        final BigDecimal ytdUnits = BigDecimal.valueOf(ytdUnitsL);
        final BigDecimal trxBd    = ytdTrx > 0 ? BigDecimal.valueOf(ytdTrx) : BigDecimal.ZERO;

        /* ---------------------------------------------------------
         * 3) 파생 지표(자바 계산)
         *    - ADS = sales / trx
         *    - UPT = units / trx
         *    - AUR = sales / units
         *    - 모든 나눗셈은 divOrZero로 0-분모 보호, 스케일 통일
         * --------------------------------------------------------- */
        final BigDecimal ads = divOrZero(ytdSales, trxBd, 2);
        final BigDecimal upt = divOrZero(ytdUnits, trxBd, 6);
        final BigDecimal aur = divOrZero(ytdSales, ytdUnits, 2);

        /* ---------------------------------------------------------
         * 4) 비교 지표
         *    - MoM: (A/B - 1) × 100
         *    - YoY: (YTD/LYTD - 1) × 100
         *    - 분모 0 보호 + 소수점 자리(1) 반올림
         * --------------------------------------------------------- */
        final BigDecimal compMoM = (bSales.signum() == 0)
                ? BigDecimal.ZERO
                : aSales.divide(bSales, 6, RoundingMode.HALF_UP)
                .subtract(BigDecimal.ONE)
                .multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP);

        final BigDecimal compYoY = (lytdSales.signum() == 0)
                ? BigDecimal.ZERO
                : ytdSales.divide(lytdSales, 6, RoundingMode.HALF_UP)
                .subtract(BigDecimal.ONE)
                .multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP);

        /* ---------------------------------------------------------
         * 5) 채널 비중(%) – YTD 매출 기준
         * --------------------------------------------------------- */
        final BigDecimal visitR = (ytdSales.signum()==0)
                ? BigDecimal.ZERO
                : visitSales.multiply(BigDecimal.valueOf(100))
                .divide(ytdSales, 1, RoundingMode.HALF_UP);

        final BigDecimal takeR  = (ytdSales.signum()==0)
                ? BigDecimal.ZERO
                : takeSales.multiply(BigDecimal.valueOf(100))
                .divide(ytdSales, 1, RoundingMode.HALF_UP);

        final BigDecimal delvR  = (ytdSales.signum()==0)
                ? BigDecimal.ZERO
                : delvSales.multiply(BigDecimal.valueOf(100))
                .divide(ytdSales, 1, RoundingMode.HALF_UP);

        /* ---------------------------------------------------------
         * 6) DTO 구성 후 반환
         * --------------------------------------------------------- */
        return KpiCardsDto.builder()
                .sales(ytdSales).transaction(ytdTrx)
                .upt(upt).ads(ads).aur(aur)
                .compMoM(compMoM).compYoY(compYoY)
                .visitRatio(visitR).takeoutRatio(takeR).deliveryRatio(delvR)
                .build();
    }


    /**
     * KPI 목록(일별/월별)을 조회한다. (페이징 지원)
     *
     * <p>
     * 이 메서드는 클라이언트에서 전달한 조회조건(cond)과 Pageable을 바탕으로
     * 점포별·기간별 KPI 행(매출, 거래수, UPT, ADS, AUR, Comp 등)을 반환한다.
     * 결과는 per-store 기반의 집계 행을 기준으로 하며, {@code showTotal=true}일 경우
     * 동일 라벨(일/월)별로 Total 행을 생성하여 각 블록 위에 삽입한다.
     * </p>
     *
     * <h3>주요 로직</h3>
     * <ol>
     *   <li>조회 모드 판별: 일별/월별 (cond.getViewBy())</li>
     *   <li>라벨 포맷 결정: 월별 → {@code "%Y-%m"}, 일별 → {@code "%Y-%m-%d"}</li>
     *   <li>기본 필터(eqKpiFilter)로 상태/기간/점포 조건 생성</li>
     *   <li>총 행수(countKpi)를 별도 호출하여 페이징 처리(페이징 기준은 per-store 행 기준)</li>
     *   <li>본문 쿼리: per-store 집계(매출 합계, 주문수) 수행 → DTO 매핑</li>
     *   <li>수량 합계는 별도 쿼리(unitRows)로 조회하여 맵핑 (cod 테이블에서 집계)</li>
     *   <li>Comp(MoM/YoY)는 선택점포 여부에 따라 {@link #computeCompByStore} 또는
     *       {@link #computeGlobalComp}를 호출하여 산출</li>
     *   <li>{@code showTotal=true}이면 라벨별 Total 행(합계)을 생성하여 결과 리스트에 삽입</li>
     * </ol>
     *
     * <h3>성능·운영 노트</h3>
     * <ul>
     *   <li>모든 SELECT는 DTO Projection으로 수행하여 엔티티 로딩을 방지</li>
     *   <li>페이지네이션은 per-store 집계 결과에 대해 offset/limit을 적용</li>
     *   <li>집계에서 수량 합계는 별도 그룹쿼리로 분리하여 큰 조인 비용을 피함</li>
     *   <li>복수 점포 선택 시 Store 별 Comp 계산은 별도 그룹쿼리로 병렬 처리 가능(현재는 단건 실행)</li>
     * </ul>
     *
     * @param cond 페이징 및 필터(조회 시작/종료일, 점포 목록, viewBy, showTotal 등)
     * @param pageable 페이징 및 정렬 정보 (offset, pageSize 등)
     * @return 요청한 페이지의 {@link KpiRowDto} 리스트를 포함한 Page 객체
     */
    @Override
    @Transactional(readOnly = true)
    public Page<KpiRowDto> findKpi(AnalyticsSearchDto cond, Pageable pageable) {

        /* ---------------------------------------------------------
         * 0) 라벨/그룹 기준 구성
         *    - 뷰 단위: 일(%Y-%m-%d) 또는 월(%Y-%m)
         *    - labelExpr: 표시용 라벨(그룹 키에도 사용)
         *    - baseFilter: 상태/기간/점포 등 공통 KPI 필터(eqKpiFilter)
         * --------------------------------------------------------- */
        final boolean byMonth = cond.getViewBy() == ViewBy.MONTH;
        final String fmt = byMonth ? "%Y-%m" : "%Y-%m-%d";
        final StringExpression labelExpr = dateFormat(co.orderedAt, fmt);

        final BooleanExpression baseFilter = eqKpiFilter(cond, co, s); // BooleanExpression 규칙(null 자동 제외)

        // 1) total count (페이징 total) — 빠른 탈출
        final Long total = countKpi(cond);
        if (total == 0L) return new PageImpl<>(Collections.emptyList(), pageable, 0L);

        /* ---------------------------------------------------------
         * 2) 기본 행 스캔: 매출합/거래수 (store × label)
         *    - 그룹: s.id, s.name, labelExpr
         *    - 정렬: 최근시간순(label 내림차순) → 동일 라벨 내 매출 내림차순
         *      * 성능 메모: labelExpr는 DATE_FORMAT이므로 정렬 인덱스는 비효율적일 수 있음.
         *        대용량에서 병목이면 YEAR/MONTH/orderedAt 원본 컬럼 정렬로 스위치 고려.
         * --------------------------------------------------------- */
        final List<Tuple> baseRows = readHints(
                query.select(
                                s.id, s.name,
                                labelExpr,
                                co.totalPrice.sum(),    // sales
                                co.id.countDistinct()   // trx
                        )
                        .from(co)
                        .join(co.store, s)
                        .where(baseFilter)
                        .groupBy(s.id, s.name, labelExpr)
                        .orderBy(
                                labelExpr.desc(),                                    // 최근시간순
                                co.totalPrice.sum().coalesce(BigDecimal.ZERO).desc() // 동일 라벨 내 매출순
                        )
                        .offset(pageable.getOffset())
                        .limit(pageable.getPageSize())
        ).fetch();

        if (baseRows.isEmpty()) return new PageImpl<>(Collections.emptyList(), pageable, total);

        /* ---------------------------------------------------------
         * 3) 수량 집계(unitRows): (store × label)로 메뉴 수량 합계
         *    - cod 드라이빙(명시적 조인), 공통 필터는 co 기준으로 동일 적용
         *    - 정렬 불필요 → 제거(페이징과 무관)
         * --------------------------------------------------------- */
        final List<Tuple> unitRows = readHints(
                query.select(
                                s.id,
                                labelExpr,
                                cod.quantity.sum()
                        )
                        .from(cod)
                        .join(cod.order, co)
                        .join(co.store, s)
                        .where(baseFilter)
                        .groupBy(s.id, labelExpr)
        ).fetch();

        // (3-1) 빠른 매핑을 위한 키→수량 맵 구성
        record Key(Long sid, String label) {}
        final Map<Key, Integer> unitsMap = new HashMap<>();
        for (Tuple t : unitRows) {
            unitsMap.put(
                    new Key(t.get(0, Long.class), t.get(1, String.class)),
                    Optional.ofNullable(t.get(2, Integer.class)).orElse(0)
            );
        }

        // 현재 페이지 내 점포ID 수집(전월/전년 비교 계산 범위를 최소화)
        final Set<Long> pageSids = new HashSet<>();
        for (Tuple t : baseRows) pageSids.add(t.get(0, Long.class));

        // 점포 선택 상태에 따른 비교로직 분기
        final List<Long> selected = cond.getStoreIds();
        final boolean isAll   = (selected == null || selected.isEmpty());
        final boolean isMulti = (selected != null && selected.size() > 1);

        /* ---------------------------------------------------------
         * 4) 비교지표(comp MoM/YoY) 계산 전략
         *    - 전체/다중 점포 화면: 점포별 비교치(perStoreComp)만 계산
         *    - 단일 점포/선택 화면: 글로벌 비교치 한 번만 계산하여 재사용
         *    - 목적: 불필요한 재계산/조인 방지(최소 비용)
         * --------------------------------------------------------- */
        final GlobalComp globalCompForTotal = computeGlobalComp(cond);
        Map<Long, GlobalComp> perStoreComp = null;
        if (isAll || isMulti) {
            perStoreComp = computeCompByStore(pageSids);
        }

        /* ---------------------------------------------------------
         * 5) DTO 매핑 + 파생지표 계산(자바)
         *    - ADS = sales / trx
         *    - UPT = units / trx
         *    - AUR = sales / units
         *    - 분모 0 보호(divOrZero) + 스케일 통일
         * --------------------------------------------------------- */
        final List<KpiRowDto> storeContent = new ArrayList<>(baseRows.size());
        for (Tuple t : baseRows) {
            final Long       sid    = t.get(0, Long.class);
            final String     sname  = t.get(1, String.class);
            final String     label  = t.get(2, String.class);
            final BigDecimal sales  = Optional.ofNullable(t.get(3, BigDecimal.class)).orElse(BigDecimal.ZERO);
            final long       trx    = Optional.ofNullable(t.get(4, Long.class)).orElse(0L);

            final int        unitsI = unitsMap.getOrDefault(new Key(sid, label), 0);
            final BigDecimal units  = BigDecimal.valueOf((long) unitsI);
            final BigDecimal trxBd  = trx > 0 ? BigDecimal.valueOf(trx) : BigDecimal.ZERO;

            final BigDecimal ads = divOrZero(sales, trxBd, 2);
            final BigDecimal upt = divOrZero(units, trxBd, 6);
            final BigDecimal aur = divOrZero(sales, units, 2);

            final GlobalComp compVal = (isAll || isMulti)
                    ? perStoreComp.getOrDefault(sid, GlobalComp.ZERO)
                    : globalCompForTotal;

            storeContent.add(KpiRowDto.builder()
                    .date(label)
                    .storeName(sname)
                    .sales(sales)
                    .transaction(trx)
                    .upt(upt)
                    .ads(ads)
                    .aur(aur)
                    .compMoM(compVal.compMoM.setScale(1, RoundingMode.HALF_UP))
                    .compYoY(compVal.compYoY.setScale(1, RoundingMode.HALF_UP))
                    .build());
        }

        /* ---------------------------------------------------------
         * 6) Total 행(라벨 단위 합계) 옵션
         *    - label별로 묶어 합계 계산 후 맨 위 Total 추가
         *    - 페이징 정렬과 충돌 없도록 자바 정렬 사용(100건 이하 가정)
         * --------------------------------------------------------- */
        if (Boolean.TRUE.equals(cond.getShowTotal())) {
            // 6-1) 라벨별 그룹핑(표시 정렬: 최신 라벨 우선, 라벨 내 매출 내림차순)
            final Map<String, List<KpiRowDto>> byLabel = new LinkedHashMap<>();
            storeContent.sort(
                    Comparator.comparing(KpiRowDto::getDate).reversed()
                            .thenComparing(
                                    (KpiRowDto r) -> r.getSales() == null ? BigDecimal.ZERO : r.getSales(),
                                    Comparator.reverseOrder()
                            )
            );
            for (KpiRowDto r : storeContent) {
                byLabel.computeIfAbsent(r.getDate(), k -> new ArrayList<>()).add(r);
            }

            // 6-2) 라벨별 수량 합계 계산(Key → label 매핑 활용)
            final Map<String, Long> unitsByLabel = new HashMap<>();
            for (Map.Entry<Key, Integer> e : unitsMap.entrySet()) {
                unitsByLabel.merge(e.getKey().label(), e.getValue().longValue(), Long::sum);
            }

            // 6-3) Total 생성 + 병합
            final List<KpiRowDto> out = new ArrayList<>();
            for (Map.Entry<String, List<KpiRowDto>> e : byLabel.entrySet()) {
                final String label = e.getKey();
                final List<KpiRowDto> rows = e.getValue();

                final BigDecimal sumSales = rows.stream()
                        .map(x -> x.getSales() == null ? BigDecimal.ZERO : x.getSales())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                final long sumTrx = rows.stream().mapToLong(x -> x.getTransaction() == null ? 0L : x.getTransaction()).sum();
                final long sumUnits = unitsByLabel.getOrDefault(label, 0L);

                final BigDecimal trxBd   = sumTrx > 0 ? BigDecimal.valueOf(sumTrx) : BigDecimal.ZERO;
                final BigDecimal unitsBd = BigDecimal.valueOf(sumUnits);

                final KpiRowDto totalRow = KpiRowDto.builder()
                        .date(label)
                        .storeName("Total")
                        .sales(sumSales)
                        .transaction(sumTrx)
                        .upt(divOrZero(unitsBd, trxBd, 6))
                        .ads(divOrZero(sumSales, trxBd, 2))
                        .aur(divOrZero(sumSales, unitsBd, 2))
                        .compMoM(globalCompForTotal.compMoM.setScale(1, RoundingMode.HALF_UP))
                        .compYoY(globalCompForTotal.compYoY.setScale(1, RoundingMode.HALF_UP))
                        .build();

                out.add(totalRow);
                out.addAll(rows);
            }
            return new PageImpl<>(out, pageable, total);
        }

        // 7) 기본 결과 반환
        return new PageImpl<>(storeContent, pageable, total);
    }


    /**
     * 주문 요약 카드 데이터를 조회한다.
     *
     * <p>
     * 본 메서드는 YTD(올해 1/1 ~ 어제)를 기준으로 주문 관련 요약 카드 정보를 구성한다.
     * 카드에는 총 트랜잭션 수와 채널별 매출(방문/포장/배달), 상위 메뉴 Top3, 카테고리별 집계(수량·매출) 등이 포함된다.
     * 결과는 {@link OrdersCardsDto}로 반환되며, 프론트에서 카드와 차트·목록에 바로 사용할 수 있도록 구조화되어 있다.
     * </p>
     *
     * <h3>계산 항목</h3>
     * <ul>
     *   <li><b>Transaction</b> – YTD 주문 건수</li>
     *   <li><b>Visit/Takeout/Delivery Sales</b> – 채널별 YTD 매출</li>
     *   <li><b>Top Menus</b> – 메뉴별 판매량/매출 상위 3개 (cod → menu 집계)</li>
     *   <li><b>Category Aggregation</b> – 카테고리별 판매수량 및 매출 (ID 단위로 대용량 집계 후 이름 매핑)</li>
     * </ul>
     *
     * <h3>쿼리/성능 노트</h3>
     * <ul>
     *   <li>트랜잭션·채널 집계는 {@code customer_order} 단일 스캔에서 SUM(CASE) 형태로 동시 계산</li>
     *   <li>Top 메뉴 집계는 {@code customer_order → customer_order_detail → menu}로 조인하되,
     *       cod 테이블에 커버링 인덱스가 존재하면 효율적으로 집계됨</li>
     *   <li>카테고리 집계는 대용량 단계에서 먼저 ID 단위로 집계한 뒤, 필요한 카테고리 이름만 별도로 조회하여 매핑함으로써
     *       불필요한 대규모 조인을 회피함</li>
     *   <li>모든 조회에 {@link #readHints(JPAQuery)}를 적용하여 readOnly/flushMode/timeout 힌트를 설정</li>
     * </ul>
     *
     * @return {@link OrdersCardsDto} 주문 요약 카드 데이터 (TopMenus, CategoriesByCount, CategoriesBySales 등 포함)
     */
    @Override
    @Transactional(readOnly = true)
    public OrdersCardsDto findOrdersSummary() {

        /* ---------------------------------------------------------
         * 0) 공통 기간(YTD) 계산 및 공통 표현식 준비
         *    - today: Asia/Seoul 기준
         *    - YTD: 당해 1/1 ~ 어제 23:59:59(닫힌-열린 >=,<)
         * --------------------------------------------------------- */
        final var today    = LocalDate.now(ZONE_SEOUL);
        final var ytdStart = LocalDate.of(today.getYear(), 1, 1);
        final var ytdEnd   = today.minusDays(1);

        final NumberExpression<Long>       SUM_QTY = Expressions.numberTemplate(Long.class,       "COALESCE(SUM({0}),0)", cod.quantity);
        final NumberExpression<BigDecimal> SUM_AMT = Expressions.numberTemplate(BigDecimal.class, "COALESCE(SUM({0}),0)", cod.lineTotal);

        // 공통 WHERE: 상태 완료 + YTD (BooleanExpression: null 자동제외 규칙)
        final BooleanExpression ytd = co.status.eq(OrderStatus.COMPLETED)
                .and(betweenDateClosedOpen(co.orderedAt, ytdStart, ytdEnd));

        /* ---------------------------------------------------------
         * 1) 트랜잭션 수 + 채널별 매출 (co 단일 스캔)
         *    - sumIf(조건, 금액)으로 채널별 매출을 한 번에 계산
         *    - 정렬 불필요 → ORDER BY NULL로 filesort 방지
         * --------------------------------------------------------- */
        OrdersCardsDto dto = timed("1. txn & channel sales", () ->
                readHints(
                        query.select(Projections.bean(OrdersCardsDto.class,
                                        co.id.count().as("transaction"),
                                        sumIf(co.orderType.eq(OrderType.VISIT),    co.totalPrice).as("visitSales"),
                                        sumIf(co.orderType.eq(OrderType.TAKEOUT),  co.totalPrice).as("takeoutSales"),
                                        sumIf(co.orderType.eq(OrderType.DELIVERY), co.totalPrice).as("deliverySales")
                                ))
                                .from(co)
                                .where(ytd)              // 기간/상태는 여기서만 공통 적용
                                .orderBy(orderByNull())  // 내부 집계용 → 정렬 제거
                ).fetchOne()
        );
        if (dto == null) dto = new OrdersCardsDto();

        /* ---------------------------------------------------------
         * 2) 메뉴 Top3 (co → cod → m)
         *    - 드라이빙: co(기간/상태 필터를 co에만 적용해 범위 축소)
         *    - 필요 컬럼만 SELECT(커버링 인덱스 활용 가정)
         *    - TOP-N 필요하므로 금액 내림차순 정렬 + limit 3
         * --------------------------------------------------------- */
        final List<Tuple> topMenusT = timed("2. top3 menus", () -> readHints(
                query.select(
                                m.menuId,
                                m.menuName,
                                SUM_QTY,
                                SUM_AMT
                        )
                        .from(co)
                        .join(cod).on(cod.order.eq(co))   // FK(order_id) 사용(명시적 조인)
                        .join(cod.menuIdFk, m)
                        .where(ytd)                       // 공통 WHERE 재사용
                        .groupBy(m.menuId, m.menuName)
                        .orderBy(SUM_AMT.desc())          // Top-N: 매출 기준 내림차순
                        .limit(3)
        ).fetch());

        final List<TopMenuItem> topMenus = new ArrayList<>(topMenusT.size());
        for (Tuple r : topMenusT) {
            topMenus.add(TopMenuItem.builder()
                    .menuId(  r.get(0, Long.class))
                    .menuName(r.get(1, String.class))
                    .quantity(Optional.ofNullable(r.get(2, Long.class)).orElse(0L))
                    .sales(   Optional.ofNullable(r.get(3, BigDecimal.class)).orElse(BigDecimal.ZERO))
                    .build());
        }
        dto.setTopMenus(topMenus);

        /* ---------------------------------------------------------
         * 3) 카테고리 집계(병목제거 3단계)
         *    3a) 대량 구간: co→cod→m까지만 조인하여 category_id 기준 집계
         *        - mc 조인을 배제해 조인 폭/비용 최소화
         *        - 정렬 불필요 → ORDER BY NULL
         *    3b) 소량 구간: 집계에 등장한 소수의 category_id에 대해서만 mc에서 이름 조회
         *    3c) 결과 매핑: 이름 매핑 + 정렬(건수/매출) + 총 판매수량(menuCount) 산출
         * --------------------------------------------------------- */

        // 3a) id-only 집계
        final List<Tuple> catAggCore = timed("3a. cat agg (id-only)", () -> readHints(
                query.select(
                                m.menuCategory.menuCategoryId,  // 카테고리 ID만
                                SUM_QTY,
                                SUM_AMT
                        )
                        .from(co)
                        .join(cod).on(cod.order.eq(co))        // co → cod
                        .join(cod.menuIdFk, m)                 // → m
                        .where(ytd)                            // 공통 WHERE 재사용
                        .groupBy(m.menuCategory.menuCategoryId)
                        .orderBy(orderByNull())
        ).fetch());

        // 3b) 이름 붙이기(등장 ID만 소량 조회)
        final var catIds = catAggCore.stream()
                .map(t -> t.get(0, Long.class))
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());

        final Map<Long, String> catNameMap = catIds.isEmpty() ? Map.of() : timed("3b. load mc names", () -> {
            final List<Tuple> names = readHints(
                    query.select(mc.menuCategoryId, mc.menuCategoryName)
                            .from(mc)
                            .where(mc.menuCategoryId.in(catIds))
                            .orderBy(orderByNull())
            ).fetch();
            final Map<Long, String> map = new HashMap<>(names.size() * 2);
            for (Tuple t : names) map.put(t.get(0, Long.class), t.get(1, String.class));
            return map;
        });

        // 3c) 매핑 + 정렬 + 총합
        final List<CategoryStat> cats = new ArrayList<>(catAggCore.size());
        long totalUnits = 0L;
        for (Tuple t : catAggCore) {
            final Long       catId = t.get(0, Long.class);
            final long       units = Optional.ofNullable(t.get(1, Long.class)).orElse(0L);                 // SUM_QTY
            final BigDecimal sales = Optional.ofNullable(t.get(2, BigDecimal.class)).orElse(BigDecimal.ZERO); // SUM_AMT
            cats.add(CategoryStat.builder()
                    .categoryId(catId)
                    .categoryName(catNameMap.getOrDefault(catId, "-"))
                    .units(units)
                    .sales(sales)
                    .build());
            totalUnits += units;
        }

        final List<CategoryStat> categoriesByCount = new ArrayList<>(cats);
        categoriesByCount.sort(Comparator.comparingLong(CategoryStat::getUnits).reversed());

        final List<CategoryStat> categoriesBySales = new ArrayList<>(cats);
        categoriesBySales.sort(Comparator.comparing(CategoryStat::getSales).reversed());

        dto.setCategoriesByCount(categoriesByCount);
        dto.setCategoriesBySales(categoriesBySales);
        dto.setMenuCount(totalUnits);

        return dto;
    }


    /**
     * 주문 목록(카테고리/메뉴/주문형태 등)을 페이징하여 조회한다.
     *
     * <p>
     * 본 메서드는 조회조건(cond)의 기간·점포·뷰 모드(일별/월별)와 Pageable 정보를 바탕으로
     * 주문 상세 행(일별: 메뉴단위 / 월별: 주문단위 집계)을 반환한다.
     * 반환 결과는 페이징 적용된 리스트이며, {@code showTotal=true}일 경우 현재 페이지 라벨 범위 내에서
     * 라벨별 Total 행을 생성하여 각 블록 위에 삽입한다.
     * </p>
     *
     * <h3>조회 모드</h3>
     * <ul>
     *   <li><b>일별 (기본)</b> – 자식 단위(주문상세 / menu)로 집계: 컬럼에 OrderId, OrderDate, Category, Menu 포함</li>
     *   <li><b>월별</b> – (연,월,점포) 단위로 그룹화하여 요약 출력: OrderId/OrderDate/Category/Menu는 출력하지 않음</li>
     * </ul>
     *
     * <h3>주요 로직</h3>
     * <ol>
     *   <li>조회 필터 구성: 상태(COMPLETED) + 기간(betweenDateClosedOpen) + 선택 점포</li>
     *   <li>총 행수 산출: {@link #countOrders(AnalyticsSearchDto)} 호출 (월별/일별에 따라 계산 방식 상이)</li>
     *   <li>본문 쿼리 실행: byMonth 여부에 따라 서로 다른 Projection 및 GROUP BY로 조회</li>
     *   <li>현재 페이지 라벨 집합(pageLabels)을 산출하여 해당 라벨 범위(pageWindow)만 추가 집계(버킷 집계) 처리</li>
     *   <li>페이지 내 집계(주문수/매출/메뉴합계 등)는 별도 쿼리로 병합하여 N+1과 불필요한 대규모 조인을 방지</li>
     *   <li>{@code showTotal=true}이면 라벨별 detailTotals/orderTotals를 조회하여 Total 행을 생성 후 블록에 삽입</li>
     * </ol>
     *
     * <h3>성능·운영 노트</h3>
     * <ul>
     *   <li>월별 집계는 DATE 함수 호출 대신 YEAR/MONTH 정수 표현으로 그룹핑하여 인덱스 활용을 돕는다</li>
     *   <li>페이지 윈도우(pageWindow)를 사용해 현재 페이지 라벨 범위만 추가 스캔하도록 최적화</li>
     *   <li>대용량 조인이 필요한 경우(일별 메뉴단위)는 cod→menu 조인만 수행하고, 필요한 집계는 별도 쿼리로 분리</li>
     *   <li>모든 조회에 {@link #readHints(JPAQuery)} 적용(읽기 전용 힌트, flushMode, timeout)</li>
     * </ul>
     *
     * @param cond 조회 조건(점포 목록, 시작/종료일, viewBy, showTotal 등)
     * @param pageable 페이지네이션 정보 (offset, pageSize)
     * @return 요청한 페이지의 {@link OrdersRowDto} 리스트를 포함한 Page 객체
     */
    @Override
    @Transactional(readOnly = true)
    public Page<OrdersRowDto> findOrders(AnalyticsSearchDto cond, Pageable pageable) {

        // 0) 라벨 표현식 및 월별/일별 스위치
        boolean byMonth = (cond.getViewBy() == ViewBy.MONTH);
        if (cond.getViewBy() == null) byMonth = false; // 기본값: 일별

        StringExpression dayLabel   = dateFormat(co.orderedAt, "%Y-%m-%d");
        StringExpression monthLabel = dateFormat(co.orderedAt, "%Y-%m");
        NumberExpression<Integer> yExpr = Expressions.numberTemplate(Integer.class, "YEAR({0})",  co.orderedAt);
        NumberExpression<Integer> mExpr = Expressions.numberTemplate(Integer.class, "MONTH({0})", co.orderedAt);

        // 0-1) 공통 WHERE (BooleanExpression 조합: null 자동제외)
        BooleanExpression filter = co.status.eq(OrderStatus.COMPLETED);
        if (cond.getStartDate() != null || cond.getEndDate() != null) {
            filter = filter.and(betweenDateClosedOpen(co.orderedAt, cond.getStartDate(), cond.getEndDate()));
        }
        if (cond.getStoreIds() != null && !cond.getStoreIds().isEmpty()) {
            filter = filter.and(s.id.in(cond.getStoreIds()));
        }

        // 1) total count (페이징 total)
        Long total = countOrders(cond);
        if (total == 0L) return new PageImpl<>(Collections.emptyList(), pageable, 0L);

        // 2) 본문 rows: (A) 월별 또는 (B) 일별
        List<OrdersRowDto> rows;
        if (byMonth) {
            // (A) 월별: (YEAR, MONTH, STORE) 버킷. 표시 라벨은 "yyyy-MM".
            rows = readHints(
                    query.select(Projections.bean(OrdersRowDto.class,
                                    ExpressionUtils.as(monthLabel, "date"),
                                    ExpressionUtils.as(monthLabel, "orderDate"),
                                    ExpressionUtils.as(s.name, "storeName"),
                                    // 상세기준 합계(메뉴 수량/매출) → DTO 필드(menuCount/menuSales)
                                    ExpressionUtils.as(
                                            Expressions.numberTemplate(Long.class, "COALESCE(SUM({0}),0)", cod.quantity),
                                            "menuCount"
                                    ),
                                    ExpressionUtils.as(
                                            Expressions.numberTemplate(BigDecimal.class, "COALESCE(SUM({0}),0)", cod.lineTotal),
                                            "menuSales"
                                    ),
                                    ExpressionUtils.as(s.id, "storeId")
                            ))
                            .from(cod)
                            .join(cod.order, co)
                            .join(co.store, s)
                            .where(filter)
                            // 그룹핑은 파생문자열(monthLabel)이 아닌 YEAR/MONTH 정수 표현식으로 묶어 정렬 일관성/성능 확보
                            .groupBy(s.id, s.name, yExpr, mExpr)
                            // 최근월 → 과거월 (요구사항: 최근시간순)
                            .orderBy(yExpr.desc(), mExpr.desc())
                            .offset(pageable.getOffset())
                            .limit(pageable.getPageSize())
            ).fetch();

        } else {
            // (B) 일별: (STORE × CATEGORY × MENU × orderedAt(원본) × CHANNEL)
            rows = readHints(
                    query.select(Projections.bean(OrdersRowDto.class,
                                    dayLabel.as("date"),                   // 라벨만 포맷하여 표시
                                    s.name.as("storeName"),
                                    mc.menuCategoryName.as("category"),
                                    m.menuName.as("menu"),
                                    Expressions.numberTemplate(BigDecimal.class, "COALESCE(SUM({0}),0)", cod.lineTotal).as("menuSales"),
                                    Expressions.numberTemplate(Long.class,     "COALESCE(SUM({0}),0)", cod.quantity).as("menuCount"),
                                    co.orderType.stringValue().as("orderType"),
                                    dayLabel.as("orderDate"),
                                    co.id.min().as("orderId"),             // 대표 주문ID(표시용)
                                    s.id.as("storeId")
                            ))
                            .from(cod)
                            .join(cod.order, co)
                            .join(co.store, s)
                            .join(cod.menuIdFk, m)
                            .join(m.menuCategory, mc)
                            .where(filter)
                            // 날짜 컬럼에는 함수 미적용(인덱스 효율), 그룹핑 키는 원본 DATE 사용
                            .groupBy(
                                    s.id, s.name,
                                    mc.menuCategoryName, m.menuName,
                                    co.orderedAt,
                                    co.orderType
                            )
                            // 최근일 → 과거일(요구사항: 최근시간순), 동일일 내 보조정렬
                            .orderBy(co.orderedAt.desc(), s.id.asc(), m.menuName.asc())
                            .offset(pageable.getOffset())
                            .limit(pageable.getPageSize())
            ).fetch();
        }

        if (rows.isEmpty()) return new PageImpl<>(Collections.emptyList(), pageable, total);

        // 2-1) 현재 페이지 라벨 윈도우 산출(3,4 단계에서만 사용하여 재집계 범위 최소화)
        StringExpression labelKey = byMonth ? monthLabel : dayLabel;

        java.util.Set<String> pageLabels = new java.util.LinkedHashSet<>();
        for (OrdersRowDto r : rows) pageLabels.add(r.getDate());

        BooleanExpression pageWindow = null;
        if (!pageLabels.isEmpty()) {
            if (byMonth) {
                // 최소~최대 YearMonth 추출 → [minYM-01, maxYM+1-01) 윈도우
                java.time.YearMonth minYM = null, maxYM = null;
                for (String lbl : pageLabels) {
                    java.time.YearMonth ym = java.time.YearMonth.parse(lbl); // "yyyy-MM"
                    if (minYM == null || ym.isBefore(minYM)) minYM = ym;
                    if (maxYM == null || ym.isAfter(maxYM))  maxYM = ym;
                }
                if (minYM != null && maxYM != null) {
                    java.time.LocalDate start = minYM.atDay(1);
                    java.time.LocalDate endPlus1 = maxYM.plusMonths(1).atDay(1);
                    pageWindow = co.orderedAt.goe(start.atStartOfDay())
                            .and(co.orderedAt.lt(endPlus1.atStartOfDay()));
                }
            } else {
                // 최소~최대 LocalDate 추출 → [min, max+1) 윈도우
                java.time.LocalDate minD = null, maxD = null;
                for (String lbl : pageLabels) {
                    java.time.LocalDate d = java.time.LocalDate.parse(lbl); // "yyyy-MM-dd"
                    if (minD == null || d.isBefore(minD)) minD = d;
                    if (maxD == null || d.isAfter(maxD))  maxD = d;
                }
                if (minD != null && maxD != null) {
                    pageWindow = co.orderedAt.goe(minD.atStartOfDay())
                            .and(co.orderedAt.lt(maxD.plusDays(1).atStartOfDay()));
                }
            }
        }

        // 3) (2)의 행과 동일 버킷 기준으로 주문건/주문매출 합산 후 rows에 병합
        java.util.Set<Long> sids = new java.util.HashSet<>();
        for (OrdersRowDto r : rows) sids.add(r.getStoreId());

        if (byMonth) {
            // 월별: (YEAR, MONTH, STORE) 버킷 집계
            List<Tuple> bucketAgg = readHints(
                    query.select(s.id, labelKey, co.id.countDistinct(), co.totalPrice.sum())
                            .from(co).join(co.store, s)
                            .where(filter, s.id.in(sids), pageWindow)
                            .groupBy(s.id, yExpr, mExpr)
            ).fetch();

            record MBKey(Long sid, String label) {}
            Map<MBKey, Tuple> map = new HashMap<>();
            for (Tuple t : bucketAgg) map.put(new MBKey(t.get(0, Long.class), t.get(1, String.class)), t);

            for (OrdersRowDto r : rows) {
                Tuple b = map.get(new MBKey(r.getStoreId(), r.getDate()));
                if (b != null) {
                    r.setOrderCount(Optional.ofNullable(b.get(2, Long.class)).orElse(0L));
                    r.setOrderSales(Optional.ofNullable(b.get(3, BigDecimal.class)).orElse(BigDecimal.ZERO));
                }
            }
        } else {
            // 일별: (orderedAt 원본, STORE, CHANNEL) 버킷 집계
            java.util.Set<OrderType> types = new java.util.HashSet<>();
            for (OrdersRowDto r : rows) if (r.getOrderType() != null) types.add(OrderType.valueOf(r.getOrderType()));

            List<Tuple> bucketAgg = readHints(
                    query.select(s.id, labelKey, co.orderType, co.id.countDistinct(), co.totalPrice.sum())
                            .from(co).join(co.store, s)
                            .where(
                                    filter,
                                    s.id.in(sids),
                                    pageWindow,
                                    !types.isEmpty() ? co.orderType.in(new ArrayList<>(types)) : null
                            )
                            .groupBy(s.id, co.orderedAt, co.orderType) // 원본 DATE 기준
            ).fetch();

            record DBKey(Long sid, String label, OrderType type) {}
            Map<DBKey, Tuple> map = new HashMap<>();
            for (Tuple t : bucketAgg) map.put(new DBKey(t.get(0, Long.class), t.get(1, String.class), t.get(2, OrderType.class)), t);

            for (OrdersRowDto r : rows) {
                OrderType ot = (r.getOrderType() == null) ? null : OrderType.valueOf(r.getOrderType());
                Tuple b = (ot == null) ? null : map.get(new DBKey(r.getStoreId(), r.getDate(), ot));
                if (b != null) {
                    r.setOrderCount(Optional.ofNullable(b.get(3, Long.class)).orElse(0L));
                    r.setOrderSales(Optional.ofNullable(b.get(4, BigDecimal.class)).orElse(BigDecimal.ZERO));
                }
            }
        }

        // 4) Total 행(라벨 단위 합계) 추가 옵션
        if (Boolean.TRUE.equals(cond.getShowTotal())) {
            Map<String, List<OrdersRowDto>> byLabel = new LinkedHashMap<>();
            for (OrdersRowDto r : rows) byLabel.computeIfAbsent(r.getDate(), k -> new ArrayList<>()).add(r);

            // 4-A) 상세 합계(메뉴 수량/매출) — 현재 페이지 라벨 윈도우만
            Map<String, Tuple> detailTotalsByLabel = new HashMap<>();
            for (Tuple t : readHints(
                    query.select(labelKey, cod.quantity.sum(), cod.lineTotal.sum())
                            .from(cod)
                            .join(cod.order, co)
                            .join(co.store, s)
                            .where(filter, pageWindow)
                            .groupBy(byMonth ? new Expression<?>[]{ yExpr, mExpr } : new Expression<?>[]{ co.orderedAt })
                            .orderBy(orderByNull()) // filesort 회피(페이징 정렬과 무관)
            ).fetch()) {
                detailTotalsByLabel.put(t.get(0, String.class), t);
            }

            // 4-B) 주문 합계(건수/금액) — 현재 페이지 라벨 윈도우만
            Map<String, Tuple> orderTotalsByLabel = new HashMap<>();
            for (Tuple t : readHints(
                    query.select(labelKey, co.id.countDistinct(), co.totalPrice.sum())
                            .from(co).join(co.store, s)
                            .where(filter, pageWindow)
                            .groupBy(byMonth ? new Expression<?>[]{ yExpr, mExpr } : new Expression<?>[]{ co.orderedAt })
                            .orderBy(orderByNull())
            ).fetch()) {
                orderTotalsByLabel.put(t.get(0, String.class), t);
            }

            // 4-C) Total 행 생성 + 같은 라벨의 상세행 정렬/병합
            List<OrdersRowDto> out = new ArrayList<>(rows.size() + byLabel.size());
            for (Map.Entry<String, List<OrdersRowDto>> e : byLabel.entrySet()) {
                String label = e.getKey();
                Tuple d = detailTotalsByLabel.get(label);
                Tuple o = orderTotalsByLabel.get(label);

                long       menuCount  = d == null ? 0L : Optional.ofNullable(d.get(1, Integer.class)).map(Integer::longValue).orElse(0L);
                BigDecimal menuSales  = d == null ? BigDecimal.ZERO : Optional.ofNullable(d.get(2, BigDecimal.class)).orElse(BigDecimal.ZERO);
                long       orderCount = o == null ? 0L : Optional.ofNullable(o.get(1, Long.class)).orElse(0L);
                BigDecimal orderSales = o == null ? BigDecimal.ZERO : Optional.ofNullable(o.get(2, BigDecimal.class)).orElse(BigDecimal.ZERO);

                OrdersRowDto totalRow = OrdersRowDto.builder()
                        .date(label)
                        .orderDate(byMonth ? "-" : label)
                        .storeName("Total")
                        .orderId(null)
                        .category("-")
                        .menu("-")
                        .menuCount(menuCount)
                        .menuSales(menuSales)
                        .orderCount(orderCount)
                        .orderSales(orderSales)
                        .orderType("-")
                        .storeId(0L)
                        .build();

                out.add(totalRow);

                List<OrdersRowDto> list = e.getValue();
                // 같은 라벨 묶음 내 표시는 점포명 기준(필요 시 자바 정렬로 filesort 비용 회피)
                list.sort(Comparator.comparing(OrdersRowDto::getStoreName, Comparator.nullsLast(String::compareTo)));
                out.addAll(list);
            }
            return new PageImpl<>(out, pageable, total);
        }

        return new PageImpl<>(rows, pageable, total);
    }


    /**
     * 재료 요약 카드 데이터를 조회한다.
     *
     * <p>
     * 본 메서드는 Asia/Seoul 기준 YTD(당해 1/1 ~ 어제까지)를 대상으로
     * 재고/발주/원가/매출/이익/마진/회전율 등의 핵심 지표를 계산하여
     * 대시보드 카드에 바로 사용할 수 있는 형태의 {@link MaterialsCardsDto}를 반환한다.
     * </p>
     *
     * <h3>집계 범위</h3>
     * <ul>
     *   <li><b>기간</b>: {@code [올해 1월 1일, 오늘)} → 어제까지 포함</li>
     *   <li><b>대상 테이블</b>:
     *     <ul>
     *       <li>가맹점 현재 재고: {@code store_material} + {@code store_inventory}</li>
     *       <li>본사 현재 재고: {@code inventory}</li>
     *       <li>발주/원가/매출: {@code receive_order} + {@code receive_order_detail} (+ 판매가 {@code unit_price}/{@code store_material})</li>
     *       <li>사용량(출고): {@code store_inventory_out}</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * <h3>반환 항목(요약)</h3>
     * <ul>
     *   <li>currentOfficeInventoryQty, currentTotalStoreInventoryQty</li>
     *   <li>orderVolumeQty(발주 수량), usedQty(출고 수량)</li>
     *   <li>salesAmount(매출), profitAmount(이익), avgMargin(%), turnoverRate</li>
     * </ul>
     *
     * <h3>쿼리/성능 노트</h3>
     * <ul>
     *   <li>필요 집계를 각각 전용 쿼리로 수행하여 조인 폭을 최소화</li>
     *   <li>판매가 계산: {@code unit_price(유효기간 내 SELLING) → store_material.selling_price → unit_price(원가)} 우선순위</li>
     *   <li>{@link #readHints(com.querydsl.jpa.impl.JPAQuery)} 적용으로 readOnly/flushMode/timeout 힌트 부여</li>
     *   <li>NULL/0 보호: {@code COALESCE}, 내부 유틸 {@code nz}, {@code divOrZero} 사용</li>
     * </ul>
     *
     * @return {@link MaterialsCardsDto} 재료 요약 카드 데이터
     * @since 2025-11-21
     * @author 이경욱
     */
    @Override
    @Transactional(readOnly = true)
    public MaterialsCardsDto findMaterialsSummary() {
        final LocalDate today    = LocalDate.now(ZONE_SEOUL);
        final LocalDate ytdStart = LocalDate.of(today.getYear(), 1, 1);
        final LocalDate ytdEndEx = today; // 어제까지 포함 ↔ exclusive 오늘

        // 1) 가맹점 전체 재고(현재) — 불필요 조인 제거
        BigDecimal totalStoreInvQty = Optional.ofNullable(
                readHints(
                        query.select(si.quantity.sum().coalesce(BigDecimal.ZERO))
                                .from(si)
                                .orderBy(orderByNull())
                ).fetchOne()
        ).orElse(BigDecimal.ZERO);

        // 1-1) 본사 재고(현재)
        BigDecimal currentOfficeInvQty = Optional.ofNullable(
                readHints(
                        query.select(inv.quantity.sum().coalesce(BigDecimal.ZERO))
                                .from(inv)
                                .orderBy(orderByNull())
                ).fetchOne()
        ).orElse(BigDecimal.ZERO);

        // 2) YTD 발주/원가/매출 — 열에 함수 금지 + 최신 단가 선택
        final QUnitPrice up2 = new QUnitPrice("up2");

        // ro.actualDeliveryDate (DATE) → TIMESTAMP 승격
        var roDateTs = Expressions.dateTimeTemplate(
                java.time.LocalDateTime.class,
                "cast({0} as timestamp)",
                ro.actualDeliveryDate
        );
        // HQL 표준: timestampadd(DAY, 1, cast(... as timestamp))
        var roDatePlus1Ts = Expressions.dateTimeTemplate(
                java.time.LocalDateTime.class,
                "timestampadd(DAY, 1, cast({0} as timestamp))",
                ro.actualDeliveryDate
        );

        // 윈도우 조건(열에 함수 미적용, 닫힌-열린)
        BooleanExpression upWindow =
                up.material.eq(sm.material)
                        .and(up.type.eq(UnitPriceType.SELLING))
                        .and(up.validFrom.loe(roDatePlus1Ts))
                        .and(up.validTo.isNull().or(roDateTs.lt(up.validTo)));

        // 최신 validFrom 선택
        var maxValidFromSubq =
                JPAExpressions.select(up2.validFrom.max())
                        .from(up2)
                        .where(
                                up2.material.eq(sm.material),
                                up2.type.eq(UnitPriceType.SELLING),
                                up2.validFrom.loe(roDatePlus1Ts),
                                up2.validTo.isNull().or(roDateTs.lt(up2.validTo))
                        );

        BooleanExpression upPickLatest = up.validFrom.eq(maxValidFromSubq);

        // 집계 쿼리 (filesort 제거)
        Tuple t = readHints(
                query.select(
                                // 발주 수량(정밀도 보존: BigDecimal)
                                Expressions.numberTemplate(BigDecimal.class, "COALESCE(SUM({0}),0)", rod.count),
                                // 원가 합: 수량 * 입고단가
                                Expressions.numberTemplate(BigDecimal.class, "COALESCE(SUM({0} * {1}),0)", rod.count, rod.unitPrice),
                                // 매출 합: up.sellingPrice → sm.sellingPrice → rod.unitPrice
                                Expressions.numberTemplate(BigDecimal.class,
                                        "COALESCE(SUM({0} * COALESCE({1},{2},{3})),0)",
                                        rod.count, up.sellingPrice, sm.sellingPrice, rod.unitPrice)
                        )
                        .from(rod)
                        .join(rod.receiveOrder, ro)
                        .join(rod.storeMaterial, sm)
                        .leftJoin(up).on(upWindow.and(upPickLatest))
                        .where(betweenDateClosedOpen(ro.actualDeliveryDate, ytdStart, ytdEndEx)) // [start,end)
                        .orderBy(orderByNull())
        ).fetchOne();

        BigDecimal orderVolumeQtyBD = (t == null) ? BigDecimal.ZERO : nz(t.get(0, BigDecimal.class));
        BigDecimal costSum          = (t == null) ? BigDecimal.ZERO : nz(t.get(1, BigDecimal.class));
        BigDecimal sellingSum       = (t == null) ? BigDecimal.ZERO : nz(t.get(2, BigDecimal.class));

        // 3) YTD 사용량(출고)
        BigDecimal totalUsedQty = Optional.ofNullable(
                readHints(
                        query.select(io.quantity.sum().coalesce(BigDecimal.ZERO))
                                .from(io)
                                .where(betweenDateClosedOpen(io.outDate, ytdStart, ytdEndEx))
                                .orderBy(orderByNull())
                ).fetchOne()
        ).orElse(BigDecimal.ZERO);

        // 4) 파생
        BigDecimal profit    = sellingSum.subtract(costSum);
        BigDecimal avgMargin = divOrZero(profit, sellingSum, 2).multiply(BigDecimal.valueOf(100));
        BigDecimal turnover  = divOrZero(orderVolumeQtyBD, totalStoreInvQty, 2);

        // DTO 스펙이 long 필드면 변환
        long orderVolumeQty = orderVolumeQtyBD.longValue();

        return MaterialsCardsDto.builder()
                .currentOfficeInventoryQty(currentOfficeInvQty.longValue())
                .currentTotalStoreInventoryQty(totalStoreInvQty.longValue())
                .orderVolumeQty(orderVolumeQty)
                .usedQty(totalUsedQty.longValue())
                .turnoverRate(turnover)
                .salesAmount(sellingSum)
                .profitAmount(profit)
                .avgMargin(avgMargin)
                .build();
    }





    /**
     * 재료 집계 목록을 페이지 단위로 조회한다.
     *
     * <p>
     * 조회 조건({@link AnalyticsSearchDto})의 기간·점포·보기모드(일별/월별)에 따라
     * 발주 상세를 기준으로 수량/원가/매출(판매가)·이익·마진·회전율 등을 계산하여
     * 표 렌더링에 적합한 {@code Page<}{@link MaterialsRowDto}{@code >}를 반환한다.
     * </p>
     *
     * <h3>조회 모드</h3>
     * <ul>
     *   <li><b>일별({@code ViewBy.DAY})</b> …</li>
     *     (점포 × 재료 × 일자) 버킷으로 그룹핑, 대표 발주ID 포함</li>
     *   <li><b>월별({@code ViewBy.MONTH})</b> …</li>
     *     (점포 × 재료 × 연/월) 버킷으로 그룹핑, 라벨은 {@code yyyy-MM}</li>
     * </ul>
     *
     * <h3>주요 로직</h3>
     * <ol>
     *   <li>메인 집계: {@code receive_order_detail → receive_order}만으로 FK 기반 집계(대량 경로 단순화)</li>
     *   <li>2차 조회: 표시에 필요한 점포명/재료명/판매가/재고 스냅샷은 별도 소량 쿼리로 조회 후 매핑</li>
     *   <li>판매금액 = 집계수량 × 판매가(store_material.selling_price 기준)</li>
     *   <li>회전율 = 기간 사용량 대비 평균재고(스냅샷/기초 기준) 계산</li>
     *   <li>{@code showTotal=true}일 때, 현재 페이지 라벨 범위에 대해 자바 측 재집계 Total 행 삽입</li>
     * </ol>
     *
     * <h3>쿼리/성능 노트</h3>
     * <ul>
     *   <li>기간 필터: {@code betweenDateClosedOpen(start, end)} 사용(닫힌-열린, 인덱스 친화)</li>
     *   <li>정렬/그룹핑: 일별은 원본 DATE 키, 월별은 YEAR/MONTH 정수 표현식 권장</li>
     *   <li>filesort 회피: 불필요한 정렬 제거 및 {@link #readHints(com.querydsl.jpa.impl.JPAQuery)} 적용</li>
     *   <li>NULL/0 보호: {@code COALESCE}, 내부 유틸 {@code nz}, {@code divOrZero} 사용</li>
     * </ul>
     *
     * @param cond 조회 조건(점포 목록, 시작/종료일, 보기 모드, showTotal)
     * @param pageable 페이지네이션 정보
     * @return {@code Page<}{@link MaterialsRowDto}{@code >} 페이지 결과
     * @since 2025-11-21
     * @author 이경욱
     */

    @Override
    @Transactional(readOnly = true)
    public Page<MaterialsRowDto> findMaterials(AnalyticsSearchDto cond, Pageable pageable) {

        final long total = countMaterials(cond);
        if (total == 0L) return new PageImpl<>(Collections.emptyList(), pageable, 0L);

        final LocalDate start = cond.getStartDate();  // inclusive
        final LocalDate end   = cond.getEndDate();    // inclusive
        final BooleanExpression periodFilter = betweenDateClosedOpen(ro.actualDeliveryDate, start, end);
        final BooleanExpression storeFilter =
                (cond.getStoreIds() == null || cond.getStoreIds().isEmpty()) ? null : ro.store.id.in(cond.getStoreIds());

        final boolean byDay = (cond.getViewBy() == null || cond.getViewBy() == ViewBy.DAY);

        final StringExpression labelExpr = byDay
                ? dateFormat(ro.actualDeliveryDate, "%Y-%m-%d")
                : dateFormat(ro.actualDeliveryDate, "%Y-%m");

        // 일별 정렬 키(WHERE에서 함수 금지, ORDER/GROUP에서만 DATE 사용)
        final DateExpression<LocalDate> dayKey =
                Expressions.dateTemplate(LocalDate.class, "DATE({0})", ro.actualDeliveryDate);

        // ===== A) 메인 집계: rod + ro만 사용 (FK 기반) =====
        final List<Tuple> rows = readHints(
                (byDay
                        ? query.select(
                                ro.store.id,                 // 0: storeId (FK)
                                rod.storeMaterial.id,        // 1: storeMaterialId (FK)
                                labelExpr,                   // 2: 라벨
                                ro.id.min(),                 // 3: 대표 발주ID (일별만)
                                Expressions.numberTemplate(Long.class, "COALESCE(SUM({0}),0)", rod.count),                      // 4: qty
                                Expressions.numberTemplate(BigDecimal.class, "COALESCE(SUM({0} * {1}),0)", rod.count, rod.unitPrice) // 5: cost
                        )
                        .from(rod)
                        .join(rod.receiveOrder, ro)
                        .where(storeFilter, periodFilter)
                        .groupBy(ro.store.id, rod.storeMaterial.id, dayKey)
                        .orderBy(dayKey.desc(), ro.store.id.asc(), rod.storeMaterial.id.asc())
                        : query.select(
                                ro.store.id,                 // 0
                                rod.storeMaterial.id,        // 1
                                labelExpr,                   // 2
                                Expressions.numberTemplate(Long.class, "COALESCE(SUM({0}),0)", rod.count),                      // 3
                                Expressions.numberTemplate(BigDecimal.class, "COALESCE(SUM({0} * {1}),0)", rod.count, rod.unitPrice) // 4
                        )
                        .from(rod)
                        .join(rod.receiveOrder, ro)
                        .where(storeFilter, periodFilter)
                        .groupBy(ro.store.id, rod.storeMaterial.id, labelExpr)
                        .orderBy(labelExpr.desc(), ro.store.id.asc(), rod.storeMaterial.id.asc())
                )
        )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        if (rows.isEmpty()) return new PageImpl<>(Collections.emptyList(), pageable, total);

        // ===== B) 2차 맵 조회(필요한 ID만) =====
        record Key(Long sid, Long smId) {}
        record MatInfo(String name, String baseUnit, BigDecimal sellingPrice) {}

        final Set<Long> storeIds = new HashSet<>();
        final Set<Long> smIds    = new HashSet<>();
        for (Tuple t : rows) {
            storeIds.add(t.get(0, Long.class));
            smIds.add(t.get(1, Long.class));
        }

        // 점포명 맵
        final Map<Long, String> storeNameMap = readHints(
                query.select(s.id, Expressions.stringTemplate("MIN({0})", s.name))
                        .from(s)
                        .where(s.id.in(storeIds))
        ).fetch().stream().collect(Collectors.toMap(
                t -> t.get(0, Long.class),
                t -> t.get(1, String.class)
        ));

        // 재료 정보 맵 (이름/단위/판매가)
        final Map<Long, MatInfo> smMap = readHints(
                query.select(
                                sm.id,
                                Expressions.stringTemplate("COALESCE({0},{1})", sm.name, mat.name),
                                sm.baseUnit,
                                sm.sellingPrice
                        )
                        .from(sm)
                        .leftJoin(sm.material, mat)
                        .where(sm.id.in(smIds))
        ).fetch().stream().collect(Collectors.toMap(
                t -> t.get(0, Long.class),
                t -> new MatInfo(
                        t.get(1, String.class),
                        t.get(2, String.class),
                        Optional.ofNullable(t.get(3, BigDecimal.class)).orElse(BigDecimal.ZERO)
                )
        ));

        // 인벤토리 스냅샷 맵 (store×storeMaterial)
        final Map<Key, BigDecimal> invMap = new HashMap<>();
        for (Tuple invT : readHints(
                query.select(
                                s.id, sm.id,
                                sm.quantity.coalesce(BigDecimal.ZERO),        // store_material 보유 수량
                                si.quantity.sum().coalesce(BigDecimal.ZERO)   // store_inventory 합계
                        )
                        .from(sm)
                        .join(sm.store, s)
                        .leftJoin(si).on(si.storeMaterial.eq(sm))
                        .where(s.id.in(storeIds), sm.id.in(smIds))
                        .groupBy(s.id, sm.id)
        ).fetch()) {
            BigDecimal smQty  = nz(invT.get(2, BigDecimal.class));
            BigDecimal siQty  = nz(invT.get(3, BigDecimal.class));
            BigDecimal snap   = (smQty.signum() != 0) ? smQty : siQty;
            invMap.put(new Key(invT.get(0, Long.class), invT.get(1, Long.class)), snap);
        }

        // ===== C) DTO 매핑 (판매합: qty * sm.sellingPrice) =====
        final List<MaterialsRowDto> list = new ArrayList<>(rows.size());
        for (Tuple t : rows) {
            Long   storeId = t.get(0, Long.class);
            Long   smId    = t.get(1, Long.class);
            String label   = t.get(2, String.class);

            final int idxQty  = byDay ? 4 : 3;
            final int idxCost = byDay ? 5 : 4;

            long       qtyL   = Optional.ofNullable(t.get(idxQty, Long.class)).orElse(0L);
            BigDecimal qty    = BigDecimal.valueOf(qtyL);
            BigDecimal cost   = Optional.ofNullable(t.get(idxCost, BigDecimal.class)).orElse(BigDecimal.ZERO);

            MatInfo mi        = smMap.get(smId);
            String  storeNm   = storeNameMap.get(storeId);
            String  matNm     = (mi != null ? mi.name : "-");
            BigDecimal price  = (mi != null ? mi.sellingPrice : BigDecimal.ZERO);

            BigDecimal sales  = qty.multiply(price);
            BigDecimal profit = sales.subtract(cost);
            BigDecimal margin = divOrZero(profit, sales, 2).multiply(BigDecimal.valueOf(100));

            BigDecimal snapshot = invMap.getOrDefault(new Key(storeId, smId), BigDecimal.ZERO);
            BigDecimal opening  = snapshot.add(qty);
            BigDecimal avgInv   = snapshot.add(opening).divide(BigDecimal.valueOf(2), 4, RoundingMode.HALF_UP);
            BigDecimal turnover = divOrZero(qty, avgInv, 2);

            // 월별 평균사용량
            BigDecimal avgUsage;
            if (byDay) {
                avgUsage = qty;
            } else {
                int daysInMonth = 30;
                try { daysInMonth = YearMonth.parse(label).lengthOfMonth(); } catch (Exception ignore) {}
                avgUsage = divOrZero(qty, BigDecimal.valueOf(daysInMonth), 2);
            }

            list.add(MaterialsRowDto.builder()
                    .orderDate(label)
                    .store(storeNm)
                    .material(matNm)
                    .storeInventoryQty(snapshot.longValue())
                    .purchaseOrderId(byDay ? t.get(3, Long.class) : null)
                    .purchaseOrderDate(label)
                    .purchaseOrderQty(qtyL)
                    .purchaseOrderAmount(cost)
                    .turnoverRate(turnover)
                    .profit(profit)
                    .margin(margin)
                    .avgUsage(avgUsage)
                    .storeId(storeId)
                    .materialId(smId)
                    .build());
        }

        // ===== D) ShowTotal 자바 재집계(옵션) =====
        if (Boolean.TRUE.equals(cond.getShowTotal())) {
            // 라벨별 합산
            class Acc {
                long q; BigDecimal cost = BigDecimal.ZERO; BigDecimal sales = BigDecimal.ZERO;
            }
            Map<String, Acc> accByLabel = new LinkedHashMap<>();
            for (MaterialsRowDto r : list) {
                Acc a = accByLabel.computeIfAbsent(r.getOrderDate(), k -> new Acc());
                a.q    += Optional.ofNullable(r.getPurchaseOrderQty()).orElse(0L);
                a.cost  = a.cost.add(Optional.ofNullable(r.getPurchaseOrderAmount()).orElse(BigDecimal.ZERO));
                // sales = profit + cost
                a.sales = a.sales.add(Optional.ofNullable(r.getProfit()).orElse(BigDecimal.ZERO)
                        .add(Optional.ofNullable(r.getPurchaseOrderAmount()).orElse(BigDecimal.ZERO)));
            }

            // 라벨별 Total 행 + 상세행(사전식)
            Map<String, List<MaterialsRowDto>> byLabel = new LinkedHashMap<>();
            for (MaterialsRowDto r : list) byLabel.computeIfAbsent(r.getOrderDate(), k -> new ArrayList<>()).add(r);

            List<MaterialsRowDto> out = new ArrayList<>();
            for (Map.Entry<String, List<MaterialsRowDto>> e : byLabel.entrySet()) {
                String lbl = e.getKey();
                Acc a = accByLabel.get(lbl);
                BigDecimal profit = a.sales.subtract(a.cost);
                BigDecimal margin = divOrZero(profit, a.sales, 2).multiply(BigDecimal.valueOf(100));

                out.add(MaterialsRowDto.builder()
                        .orderDate(lbl)
                        .store("Total")
                        .material("-")
                        .storeInventoryQty(0L)
                        .purchaseOrderId(null)
                        .purchaseOrderDate(lbl)
                        .purchaseOrderQty(a.q)
                        .purchaseOrderAmount(a.cost)
                        .turnoverRate(BigDecimal.ZERO)
                        .profit(profit)
                        .margin(margin)
                        .avgUsage(BigDecimal.ZERO)
                        .build());

                List<MaterialsRowDto> group = e.getValue();
                group.sort(Comparator
                        .comparing(MaterialsRowDto::getStore, Comparator.nullsLast(String::compareTo))
                        .thenComparing(MaterialsRowDto::getMaterial, Comparator.nullsLast(String::compareTo)));
                out.addAll(group);
            }
            return new PageImpl<>(out, pageable, total);
        }

        return new PageImpl<>(list, pageable, total);
    }





    /**
     * 시간·요일 차트(카드)용 요약 데이터를 조회한다 (YTD).
     *
     * <p>
     * 기준일(오늘)을 기준으로 올해 1월 1일 ~ 어제까지(YTD) 범위의 시간대별(08:00~22:00)·요일별(일~토) 매출을 산출하여
     * 차트용 DTO인 {@link TimeChartCardDto}를 반환한다. 반환되는 시리즈는 OrderType 별(DELIVERY/TAKEOUT/VISIT)로 분리되어 있으며,
     * 매장 필터가 적용되지 않은 전체 합계 시리즈만 포함한다(매장별 시리즈는 필터 모드에서 {@link #findTimeChart}로 제공).
     * </p>
     *
     * <h3>출력</h3>
     * <ul>
     *   <li>hours: 08:00 ~ 22:00 라벨 리스트</li>
     *   <li>dows: "일","월",...,"토" 라벨 리스트</li>
     *   <li>timeOfDay: 시간대별 시리즈 리스트 (각 시리즈는 name, data[15])</li>
     *   <li>dayOfWeek: 요일별 시리즈 리스트 (각 시리즈는 name, data[7])</li>
     * </ul>
     *
     * <h3>성능·운영 노트</h3>
     * <ul>
     *   <li>집계는 {@code customer_order} 단일 테이블을 그룹화(HOUR/DAYOFWEEK 기준) 하여 수행</li>
     *   <li>시간대는 08~22시로 고정하여 불필요한 범위 스캔을 줄임</li>
     *   <li>전체(매장 미선택)일 경우에만 'Total - &lt;OrderType&gt;' 시리즈를 생성, 매장 선택 시에는 매장별 시리즈만 생성</li>
     *   <li>모든 쿼리에 {@link #readHints(JPAQuery)} 적용(읽기 전용 힌트, flushMode, timeout)</li>
     * </ul>
     *
     * @return YTD 범위의 {@link TimeChartCardDto} (hours, dows, timeOfDay, dayOfWeek 포함)
     */
    @Override
    @Transactional(readOnly = true)
    public TimeChartCardDto findTimeChartSummary() {

        // 0) 기준일 계산(Asia/Seoul)
        final var today    = LocalDate.now(ZONE_SEOUL);

        // 1) YTD 경계 계산: [ytdStart, ytdEndExclusive)
        final var ytdStart = LocalDate.of(today.getYear(), 1, 1);
        final var ytdEnd   = today.minusDays(1); // 어제까지 확정치만 반영

        // 2) 위임: 점포 필터(null → 전체), 내부에서 닫힌–열린 필터 사용 가정
        //    마지막 플래그(true)는 내부 옵션(예: 합계 포함, 스택/스무딩 등)을 제어하는 구현 세부사항.
        //    쿼리 힌트/CASE 집계/정렬 최적화는 buildTimeChart(...) 내부에서 일관 적용.
        return buildTimeChart(null, ytdStart, ytdEnd, true);
    }

    /**
     * 시간·요일 차트 데이터를 조회한다 (필터 적용).
     *
     * <p>
     * 사용자가 선택한 점포(복수 가능)와 기간(cond.startDate, cond.endDate)에 따라
     * 시간대별(08:00~22:00)·요일별(일~토) 매출 시리즈를 생성하여 {@link TimeChartRowDto}로 반환한다.
     * 매장 선택이 있으면 매장별·OrderType별 시리즈가 반환되고, 매장 선택이 없으면 Total - OrderType 시리즈가 반환된다.
     * </p>
     *
     * <h3>입력</h3>
     * <ul>
     *   <li>cond.getStoreIds(): 선택된 매장 ID 리스트(없으면 전체)</li>
     *   <li>cond.getStartDate(), cond.getEndDate(): 조회 기간 (inclusive start, exclusive end+1day 내부 처리)</li>
     * </ul>
     *
     * <h3>성능·운영 노트</h3>
     * <ul>
     *   <li>필터 모드(매장 선택)인 경우에만 매장별 집계를 수행하여 불필요한 대규모 결과 생성을 방지</li>
     *   <li>HOUR/DAYOFWEEK 기반 그룹화로 인덱스 활용 및 파일정렬 회피</li>
     * </ul>
     *
     * @param cond 조회조건(점포 목록, 시작일, 종료일 등)
     * @return 필터 기준의 {@link TimeChartRowDto} (hours, dows, timeOfDay, dayOfWeek 포함)
     */
    @Override
    @Transactional(readOnly = true)
    public TimeChartRowDto findTimeChart(AnalyticsSearchDto cond) {
        // 위임: 쿼리·집계·정렬·힌트는 buildTimeChart에서 처리
        // - storeIds: 선택 필터(null/빈 → 전체)
        // - start/end: 닫힌–열린([start, end)) 규약으로 내부 정규화
        // - 마지막 false: 요약 카드가 아닌 상세 차트 모드(내부 구현 옵션)
        return buildTimeChart(
                cond.getStoreIds(),
                cond.getStartDate(),
                cond.getEndDate(),
                false
        );
    }

    /**
     * 시간·요일 상세 표(상세 목록)를 페이징 조회한다.
     *
     * <p>
     * 시간·요일 분석 페이지의 상세 테이블(메뉴단위 또는 월별 주문단위)을 조회하여 페이징된
     * {@link TimeRowDto} 리스트를 반환한다.
     * - 일별 모드(기본): 주문상세 단위(cod 기준)로 행 구성(메뉴명, 카테고리, 주문금액, 주문ID, 주문일시 등 포함)
     * - 월별 모드: 주문단위/시간대별(주문 총액)로 집계하여 간략 행 반환
     * </p>
     *
     * <h3>핵심 규칙</h3>
     * <ul>
     *   <li>기간 필터는 {@link #betweenDateClosedOpen(DateTimePath, LocalDate, LocalDate)}를 사용하여 인덱스 사용을 유도</li>
     *   <li>시간대/요일 표시: 시간 슬롯("HH:00-HH:59"), 요일은 한글("일"…"토")로 변환</li>
     *   <li>정렬 우선순위는 '최근 주문시간(desc)'을 1순위로 보장하고, 블록 내부는 금액/점포명 등으로 타이브레이킹</li>
     *   <li>{@code showTotal=true}일 경우 현재 페이지 라벨 윈도우 범위 내에서 총액 Total 행(최대 3줄: OrderType 순서로)을 블록 상단에 삽입</li>
     * </ul>
     *
     * <h3>성능·운영 노트</h3>
     * <ul>
     *   <li>페이지 윈도우(pageWindow)를 계산하여 현재 페이지 라벨 범위만 추가 집계(버킷 합계)하도록 최적화</li>
     *   <li>집계 쿼리는 필요한 컬럼만 SELECT 하며, orderBy에서는 원본 날짜 컬럼을 우선으로 사용(함수 사용 자제)</li>
     *   <li>시간 슬롯/요일 변환을 Java에서 일부 처리하여 DB 부하를 낮춤</li>
     * </ul>
     *
     * @param cond 조회 조건(시작일/종료일, 점포 리스트, viewBy, showTotal 등)
     * @param pageable 페이지네이션 정보 (offset, pageSize)
     * @return 페이징된 {@link TimeRowDto} 리스트를 담은 Page 객체
     */
    @Override
    @Transactional(readOnly = true)
    public Page<TimeRowDto> findTimeRows(AnalyticsSearchDto cond, Pageable pageable) {

        /* ---------------------------------------------------------
         * 0) 공통 WHERE 구성
         *    - 상태 완료 + 기간 [start, end)
         *    - 점포 필터(null/empty 자동제외 규칙)
         * --------------------------------------------------------- */
        BooleanExpression filter = co.status.eq(OrderStatus.COMPLETED)
                .and(betweenDateClosedOpen(co.orderedAt, cond.getStartDate(), cond.getEndDate()));
        if (cond.getStoreIds() != null && !cond.getStoreIds().isEmpty()) {
            filter = filter.and(s.id.in(cond.getStoreIds()));
        }

        final boolean byMonth = (cond.getViewBy() == ViewBy.MONTH);

        /* ---------------------------------------------------------
         * 0-1) 라벨/파생식 및 정렬용 정수 표현식
         *      - 라벨은 표시만 DATE_FORMAT, 그룹/정렬은 가급적 원본 컬럼(또는 YEAR/MONTH) 사용
         *      - 시간대/요일 라벨은 문자열로 미리 만들어 가독성↑
         * --------------------------------------------------------- */
        StringExpression dayLabel   = Expressions.stringTemplate("DATE_FORMAT({0}, {1})", co.orderedAt, Expressions.constant("%Y-%m-%d"));
        StringExpression monthLabel = Expressions.stringTemplate("DATE_FORMAT({0}, {1})", co.orderedAt, Expressions.constant("%Y-%m"));

        StringExpression hourSlotExpr = Expressions.stringTemplate(
                "CONCAT(DATE_FORMAT({0}, '%H'), ':00-', DATE_FORMAT({0}, '%H'), ':59')", co.orderedAt);
        StringExpression dayNameExpr = Expressions.stringTemplate(
                "CONCAT('', ELT(DAYOFWEEK({0}), '일','월','화','수','목','금','토'))", co.orderedAt);
        StringExpression orderDateStr = Expressions.stringTemplate(
                "DATE_FORMAT({0}, {1})", co.orderedAt, Expressions.constant("%Y-%m-%d %H:%i"));

        // 정렬/그룹 최적화용 정수 파생식(원본 컬럼 기반)
        NumberExpression<Integer> Y = Expressions.numberTemplate(Integer.class, "YEAR({0})",  co.orderedAt);
        NumberExpression<Integer> M = Expressions.numberTemplate(Integer.class, "MONTH({0})", co.orderedAt);
        NumberExpression<Integer> H = Expressions.numberTemplate(Integer.class, "HOUR({0})",  co.orderedAt);
        NumberExpression<Integer> D = Expressions.numberTemplate(Integer.class, "DAYOFWEEK({0})", co.orderedAt);

        /* ---------------------------------------------------------
         * 1) total count (페이징 total) — 빠른 탈출
         * --------------------------------------------------------- */
        Long total = countTime(cond);
        if (total == 0L) return new PageImpl<>(Collections.emptyList(), pageable, 0L);

        /* ---------------------------------------------------------
         * 2) 본문 rows
         *    - 월별: 주문 단위 집계(store × YEAR/MONTH × hour × dow × channel)
         *    - 일별: 메뉴 단위 상세행(최근 주문시간순)
         * --------------------------------------------------------- */
        List<TimeRowDto> rows;
        if (byMonth) {
            // 월별: 합계금액(주문 기준)
            NumberExpression<BigDecimal> SUM_AMT =
                    Expressions.numberTemplate(BigDecimal.class, "COALESCE(SUM({0}),0)", co.totalPrice);

            rows = readHints(
                    query.select(Projections.fields(TimeRowDto.class,
                                    monthLabel.as("date"),
                                    s.name.as("storeName"),
                                    ExpressionUtils.as(hourSlotExpr, "hourSlot"),
                                    ExpressionUtils.as(dayNameExpr, "dayOfWeek"),
                                    ExpressionUtils.as(SUM_AMT, "orderAmount"),
                                    co.orderType.stringValue().as("orderType")
                            ))
                            .from(co)
                            .join(co.store, s)
                            // 날짜 범위 인덱스 스캔 이후 HOUR 필터 적용됨.
                            // 매우 대용량에서 추가 최적화가 필요하면 “기간을 더 조밀하게” 제한하는 방식 권장.
                            .where(filter, H.between(8, 22))
                            .groupBy(s.id, s.name, Y, M, H, D, co.orderType)
                            .orderBy(
                                    Y.desc(),   // 최근 월 우선
                                    M.desc(),
                                    H.desc(),   // 같은 월 안에서 최근 시간대 우선
                                    D.desc(),   // tie-break
                                    s.name.asc(),
                                    co.orderType.asc()
                            )
                            .offset(pageable.getOffset())
                            .limit(pageable.getPageSize())
            ).fetch();

        } else {
            // 일별: 상세 행(메뉴 라인 기준 표시 금액)
            NumberExpression<BigDecimal> lineAmt =
                    Expressions.numberTemplate(BigDecimal.class, "COALESCE({0},0)", cod.lineTotal);

            rows = readHints(
                    query.select(Projections.fields(TimeRowDto.class,
                                    ExpressionUtils.as(dayLabel, "date"),
                                    s.name.as("storeName"),
                                    ExpressionUtils.as(hourSlotExpr, "hourSlot"),
                                    ExpressionUtils.as(dayNameExpr, "dayOfWeek"),
                                    co.id.as("orderId"),
                                    ExpressionUtils.as(lineAmt, "orderAmount"),
                                    mc.menuCategoryName.as("category"),
                                    m.menuName.as("menu"),
                                    co.orderType.stringValue().as("orderType"),
                                    ExpressionUtils.as(orderDateStr, "orderDate")
                            ))
                            .from(cod).join(cod.order, co).join(co.store, s)
                            .join(cod.menuIdFk, m).join(m.menuCategory, mc)
                            .where(filter)
                            // 최근 주문시간순 보장(체감 요구사항)
                            .orderBy(
                                    co.orderedAt.desc(),
                                    s.name.asc(),
                                    m.menuName.asc()
                            )
                            .offset(pageable.getOffset())
                            .limit(pageable.getPageSize())
            ).fetch();
        }

        /* ---------------------------------------------------------
         * 3) Total 행 삽입(옵션)
         *    - 현재 페이지 라벨 범위를 산출(pageWindow)
         *    - (라벨 × 시간 × 요일 × OrderType) 금액을 한 번 더 집계 후,
         *      각 블록 맨 위 “Total(최대 3줄: DELV/TAKE/VISIT)” 삽입
         *    - 내부 집계는 정렬 제거(orderByNull)로 filesort 회피
         * --------------------------------------------------------- */
        if (!rows.isEmpty() && Boolean.TRUE.equals(cond.getShowTotal())) {

            // 3-1) 현재 페이지 라벨 수집
            java.util.Set<String> pageLabels = new java.util.LinkedHashSet<>();
            for (TimeRowDto r : rows) pageLabels.add(r.getDate());

            // 3-2) 라벨 윈도우(현재 페이지 범위만 재집계)
            BooleanExpression pageWindow = null;
            if (!pageLabels.isEmpty()) {
                if (byMonth) {
                    java.time.YearMonth minYM = null, maxYM = null;
                    for (String lbl : pageLabels) {
                        java.time.YearMonth ym = java.time.YearMonth.parse(lbl);
                        if (minYM == null || ym.isBefore(minYM)) minYM = ym;
                        if (maxYM == null || ym.isAfter(maxYM))  maxYM = ym;
                    }
                    if (minYM != null && maxYM != null) {
                        java.time.LocalDate start = minYM.atDay(1);
                        java.time.LocalDate endPlus1 = maxYM.plusMonths(1).atDay(1);
                        pageWindow = co.orderedAt.goe(start.atStartOfDay())
                                .and(co.orderedAt.lt(endPlus1.atStartOfDay()));
                    }
                } else {
                    java.time.LocalDate minD = null, maxD = null;
                    for (String lbl : pageLabels) {
                        java.time.LocalDate d = java.time.LocalDate.parse(lbl);
                        if (minD == null || d.isBefore(minD)) minD = d;
                        if (maxD == null || d.isAfter(maxD))  maxD = d;
                    }
                    if (minD != null && maxD != null) {
                        pageWindow = co.orderedAt.goe(minD.atStartOfDay())
                                .and(co.orderedAt.lt(maxD.plusDays(1).atStartOfDay()));
                    }
                }
            }

            // 3-3) 합계 집계: (라벨 × H × D × OrderType) 총액
            StringExpression labelKey = byMonth ? monthLabel : dayLabel;

            // label -> hour -> dow(1~7) -> type -> amt
            Map<String, Map<Integer, Map<Integer, Map<OrderType, BigDecimal>>>> sumMap = new LinkedHashMap<>();

            JPAQuery<Tuple> tq = query.select(
                            labelKey,
                            H, D, co.orderType,
                            Expressions.numberTemplate(BigDecimal.class, "COALESCE(SUM({0}),0)", co.totalPrice)
                    )
                    .from(co)
                    .join(co.store, s)
                    .where(filter, pageWindow, H.goe(8).and(H.loe(22)));

            if (byMonth) {
                tq.groupBy(Y, M, H, D, co.orderType).orderBy(orderByNull());
            } else {
                tq.groupBy(co.orderedAt, H, D, co.orderType).orderBy(orderByNull());
            }

            for (Tuple t : readHints(tq).fetch()) {
                String     lbl = t.get(0, String.class);
                Integer    h   = t.get(1, Integer.class);
                Integer    d   = t.get(2, Integer.class);     // 1=일 ... 7=토
                OrderType  ot  = t.get(3, OrderType.class);
                BigDecimal amt = Optional.ofNullable(t.get(4, BigDecimal.class)).orElse(BigDecimal.ZERO);

                if (lbl == null || h == null || d == null || ot == null) continue;
                if (d < 1 || d > 7) continue;

                sumMap
                        .computeIfAbsent(lbl, k -> new LinkedHashMap<>())
                        .computeIfAbsent(h, k -> new LinkedHashMap<>())
                        .computeIfAbsent(d, k -> new EnumMap<>(OrderType.class))
                        .merge(ot, amt, BigDecimal::add);
            }

            // 3-4) 기존 rows를 라벨 → (시간대,요일) 그룹으로 묶고 각 블록 위에 Total 꽂기
            Map<String, List<TimeRowDto>> byLabelAll = new LinkedHashMap<>();
            for (TimeRowDto r : rows) {
                byLabelAll.computeIfAbsent(r.getDate(), k -> new ArrayList<>()).add(r);
            }

            List<TimeRowDto> out = new ArrayList<>(rows.size());

            // 라벨 정렬: 최신 → 과거(월/일 각각 파싱)
            List<Map.Entry<String, List<TimeRowDto>>> labelEntries = new ArrayList<>(byLabelAll.entrySet());
            labelEntries.sort((e1, e2) -> {
                if (byMonth) {
                    YearMonth a = YearMonth.parse(e1.getKey());
                    YearMonth b = YearMonth.parse(e2.getKey());
                    return b.compareTo(a); // desc
                } else {
                    LocalDate a = LocalDate.parse(e1.getKey());
                    LocalDate b = LocalDate.parse(e2.getKey());
                    return b.compareTo(a); // desc
                }
            });

            for (Map.Entry<String, List<TimeRowDto>> labelEntry : labelEntries) {
                String label = labelEntry.getKey();
                List<TimeRowDto> labelRows = labelEntry.getValue();

                // (시간대, 요일) 그룹
                Map<String, List<TimeRowDto>> groups = new LinkedHashMap<>();
                for (TimeRowDto r : labelRows) {
                    String key = (r.getHourSlot()==null ? "-" : r.getHourSlot()) + "|" + (r.getDayOfWeek()==null ? "-" : r.getDayOfWeek());
                    groups.computeIfAbsent(key, k -> new ArrayList<>()).add(r);
                }

                // 그룹 정렬: 시간 22→08, 같은 시간대 내 요일 토(7)→일(1)
                List<Map.Entry<String, List<TimeRowDto>>> gList = new ArrayList<>(groups.entrySet());
                gList.sort(
                        Comparator
                                .comparingInt((Map.Entry<String, List<TimeRowDto>> kv) ->
                                        hourFromSlot(kv.getValue().get(0).getHourSlot())
                                ).reversed()
                                .thenComparing(
                                        Comparator.comparingInt((Map.Entry<String, List<TimeRowDto>> kv) ->
                                                dowIntFromKo(kv.getValue().get(0).getDayOfWeek())
                                        ).reversed()
                                )
                );

                Map<Integer, Map<Integer, Map<OrderType, BigDecimal>>> hMap =
                        sumMap.getOrDefault(label, Map.of());

                for (var g : gList) {
                    List<TimeRowDto> block = g.getValue();
                    if (block.isEmpty()) continue;

                    // 블록 대표값(모든 행 동일)
                    String hourSlot = block.get(0).getHourSlot();
                    String dowKo    = block.get(0).getDayOfWeek();

                    int hVal = hourFromSlot(hourSlot);
                    int dVal = dowIntFromKo(dowKo);

                    Map<OrderType, BigDecimal> tMap =
                            hMap.getOrDefault(hVal, Map.of()).getOrDefault(dVal, Map.of());

                    // OrderType 고정 순서(DELIVERY → TAKEOUT → VISIT)로 최대 3줄 Total 삽입
                    for (OrderType ot : new OrderType[]{OrderType.DELIVERY, OrderType.TAKEOUT, OrderType.VISIT}) {
                        BigDecimal amt = tMap.get(ot);
                        if (amt == null || amt.signum()==0) continue;

                        out.add(TimeRowDto.builder()
                                .date(label)
                                .storeName("Total")
                                .hourSlot(hourSlot)
                                .dayOfWeek(dowKo)
                                .orderId(null)
                                .orderAmount(amt)
                                .category("-")
                                .menu("-")
                                .orderType(ot.name())
                                .orderDate(label)
                                .build());
                    }

                    // 매장 행 정렬: 금액 desc → 점포명 → OrderType (가독성)
                    block.sort(Comparator
                            .comparing((TimeRowDto r) -> r.getOrderAmount()==null ? BigDecimal.ZERO : r.getOrderAmount(), Comparator.reverseOrder())
                            .thenComparing(TimeRowDto::getStoreName, Comparator.nullsLast(String::compareTo))
                            .thenComparing(TimeRowDto::getOrderType, Comparator.nullsLast(String::compareTo))
                    );
                    out.addAll(block);
                }
            }

            return new PageImpl<>(out, pageable, total);
        }

        // 4) 기본 결과 반환
        return new PageImpl<>(rows, pageable, total);
    }

    /* ===================== 내부 빌더: 차트 공용 ===================== */

    /**
     * 시간·요일 차트용 시리즈를 조립하여 반환한다.
     *
     * <p>
     * 이 내부 공용 메서드는 다음 두 가지 모드로 동작한다:
     * <ul>
     *   <li>YTD 카드 모드: {@code ytdMode=true} — 매장 필터 없이 전체 합계(총합) 시리즈를 반환 (TimeChartCardDto)</li>
     *   <li>필터 모드: {@code ytdMode=false} — 매장 선택(storeIds)이 있으면 매장별 시리즈, 없으면 Total - OrderType 시리즈를 반환 (TimeChartRowDto)</li>
     * </ul>
     * </p>
     *
     * <h3>설계/성능 노트</h3>
     * <ul>
     *   <li>시간 범위는 08:00~22:00으로 고정(불필요한 스캔 감소)</li>
     *   <li>DB는 HOUR/DAYOFWEEK로 그룹화하여 인덱스 사용을 방해하지 않음</li>
     *   <li>쿼리 결과는 작은 메모리 구조(LinkedHashMap → 리스트)로 변환하여 프론트에 전달</li>
     *   <li>쿼리 실행에는 {@link #readHints(JPAQuery)}가 적용되어 timeout/readonly 힌트 설정</li>
     * </ul>
     *
     * @param storeIds 매장 필터(없으면 전체) — null 또는 빈 리스트 허용
     * @param start 조회 시작일 (inclusive)
     * @param end 조회 종료일 (inclusive) — 내부적으로 end+1(00:00) 미만으로 처리하는 where절 사용
     * @param ytdMode true면 카드(YTD) 모드, false면 필터 모드 반환
     * @param <T> 반환 타입: ytdMode=true → {@link TimeChartCardDto}, 그렇지 않으면 {@link TimeChartRowDto}
     * @return 차트 DTO (제네릭) — 호출부에서 적절히 캐스팅하여 사용
     */
    @SuppressWarnings("unchecked")
    private <T> T buildTimeChart(List<Long> storeIds, LocalDate start, LocalDate end, boolean ytdMode) {

        /* ---------------------------------------------------------
         * 0) 축 준비: 시간(08~22)·요일(일~토)
         *    - 차트 X축에 그대로 사용되며, 시리즈 데이터는 인덱스 기반으로 매핑된다.
         *      hour index = (hour - 8), dow index = (dow(1~7) - 1)
         * --------------------------------------------------------- */
        List<String> hours = new java.util.ArrayList<>();
        for (int h = 8; h <= 22; h++) hours.add(String.format("%02d:00", h));
        List<String> dows = java.util.List.of("일","월","화","수","목","금","토");

        /* ---------------------------------------------------------
         * 1) 공통 WHERE
         *    - 상태 완료 + 기간 [start, end)
         *    - 필터 모드(ytdMode=false) && 점포선택 존재 시 점포 IN 추가
         * --------------------------------------------------------- */
        BooleanExpression base = co.status.eq(OrderStatus.COMPLETED)
                .and(betweenDateClosedOpen(co.orderedAt, start, end));

        boolean filteredStores = (!ytdMode && storeIds != null && !storeIds.isEmpty());
        if (filteredStores) {
            base = base.and(s.id.in(storeIds));
        }

        /* ---------------------------------------------------------
         * 2) 집계 차원 파생식
         *    - 날짜 인덱스로 범위를 줄인 후 HOUR/DAYOFWEEK 파생을 사용해 그룹핑한다.
         * --------------------------------------------------------- */
        NumberExpression<Integer> H = Expressions.numberTemplate(Integer.class, "HOUR({0})",       co.orderedAt);
        NumberExpression<Integer> D = Expressions.numberTemplate(Integer.class, "DAYOFWEEK({0})",  co.orderedAt);

        /* ---------------------------------------------------------
         * 3) 전체 합계(시간/요일 × OrderType)
         *    - “전체 선택 or YTD 카드 모드”일 때만 시리즈로 사용된다.
         *    - 정렬 불필요 집계는 filesort 방지를 위해 orderByNull 적용.
         * --------------------------------------------------------- */
        List<Tuple> hourTotal = readHints(
                query.select(H, co.orderType, co.totalPrice.sum())
                        .from(co).join(co.store, s)
                        .where(base)
                        .groupBy(H, co.orderType)
                        .orderBy(orderByNull())
        ).fetch();

        List<Tuple> dowTotal = readHints(
                query.select(D, co.orderType, co.totalPrice.sum())
                        .from(co).join(co.store, s)
                        .where(base)
                        .groupBy(D, co.orderType)
                        .orderBy(orderByNull())
        ).fetch();

        /* ---------------------------------------------------------
         * 4) 점포별 합계(필터 모드에서만)
         *    - “{StoreName} - {OrderType}” 시리즈를 구성한다.
         * --------------------------------------------------------- */
        List<Tuple> hourByStore = Collections.emptyList();
        List<Tuple> dowByStore  = Collections.emptyList();
        if (filteredStores) {
            hourByStore = readHints(
                    query.select(s.id, s.name, H, co.orderType, co.totalPrice.sum())
                            .from(co).join(co.store, s)
                            .where(base)
                            .groupBy(s.id, s.name, H, co.orderType)
                            .orderBy(orderByNull())
            ).fetch();

            dowByStore = readHints(
                    query.select(s.id, s.name, D, co.orderType, co.totalPrice.sum())
                            .from(co).join(co.store, s)
                            .where(base)
                            .groupBy(s.id, s.name, D, co.orderType)
                            .orderBy(orderByNull())
            ).fetch();
        }

        /* ---------------------------------------------------------
         * 5) 시리즈 버퍼(0 초기화)
         *    - 시간대 15칸(08~22), 요일 7칸(일~토)
         *    - LinkedHashMap으로 시리즈 삽입 순서를 유지(차트 범례/표시 순서 안정)
         * --------------------------------------------------------- */
        Map<String, List<BigDecimal>> hourSeries = new LinkedHashMap<>();
        Map<String, List<BigDecimal>> dowSeries  = new LinkedHashMap<>();

        Supplier<List<BigDecimal>> hourZeros = () -> {
            var z = new ArrayList<BigDecimal>(15);
            for (int i = 0; i < 15; i++) z.add(BigDecimal.ZERO);
            return z;
        };
        Supplier<List<BigDecimal>> dowZeros = () -> {
            var z = new ArrayList<BigDecimal>(7);
            for (int i = 0; i < 7; i++) z.add(BigDecimal.ZERO);
            return z;
        };

        /* ---------------------------------------------------------
         * 6) “전체 선택 or YTD 카드 모드” → Total 시리즈 생성
         *    - key: "Total - {OrderType}"
         *    - 인덱스 매핑: hour → (h-8), dow → (d-1)
         * --------------------------------------------------------- */
        if (!filteredStores) {
            for (Tuple t : hourTotal) {
                Integer h = t.get(0, Integer.class);
                if (h == null || h < 8 || h > 22) continue;              // 범위 밖 방어
                OrderType ot = t.get(1, OrderType.class);
                String key = "Total - " + (ot == null ? "UNKNOWN" : ot.name());
                var arr = hourSeries.computeIfAbsent(key, k -> hourZeros.get());
                arr.set(h - 8, nz(t.get(2, BigDecimal.class)));
            }
            for (Tuple t : dowTotal) {
                Integer d = t.get(0, Integer.class);
                if (d == null || d < 1 || d > 7) continue;               // 1=일 … 7=토
                OrderType ot = t.get(1, OrderType.class);
                String key = "Total - " + (ot == null ? "UNKNOWN" : ot.name());
                var arr = dowSeries.computeIfAbsent(key, k -> dowZeros.get());
                arr.set(d - 1, nz(t.get(2, BigDecimal.class)));
            }
        }

        /* ---------------------------------------------------------
         * 7) “점포 필터 모드” → 점포별 시리즈 생성
         *    - key: "{StoreName} - {OrderType}"
         * --------------------------------------------------------- */
        for (Tuple t : hourByStore) {
            String sname = t.get(1, String.class);
            Integer h    = t.get(2, Integer.class);
            if (h == null || h < 8 || h > 22) continue;
            OrderType ot = t.get(3, OrderType.class);
            String key = (sname == null ? "-" : sname) + " - " + (ot == null ? "UNKNOWN" : ot.name());
            var arr = hourSeries.computeIfAbsent(key, k -> hourZeros.get());
            arr.set(h - 8, nz(t.get(4, BigDecimal.class)));
        }

        for (Tuple t : dowByStore) {
            String sname = t.get(1, String.class);
            Integer d    = t.get(2, Integer.class);
            if (d == null || d < 1 || d > 7) continue;
            OrderType ot = t.get(3, OrderType.class);
            String key = (sname == null ? "-" : sname) + " - " + (ot == null ? "UNKNOWN" : ot.name());
            var arr = dowSeries.computeIfAbsent(key, k -> dowZeros.get());
            arr.set(d - 1, nz(t.get(4, BigDecimal.class)));
        }

        /* ---------------------------------------------------------
         * 8) DTO 구성 및 반환
         *    - 시리즈 Map → List로 변환(삽입 순서 유지)
         *    - ytdMode: 카드 DTO, else: 표 상세 DTO
         * --------------------------------------------------------- */
        var hourOut = new ArrayList<ChartSeriesDto>();
        hourSeries.forEach((k, v) -> hourOut.add(ChartSeriesDto.builder().name(k).data(v).build()));

        var dowOut  = new ArrayList<ChartSeriesDto>();
        dowSeries.forEach((k, v) -> dowOut.add(ChartSeriesDto.builder().name(k).data(v).build()));

        if (ytdMode) {
            return (T) TimeChartCardDto.builder()
                    .hours(hours).dows(dows)
                    .timeOfDay(hourOut)
                    .dayOfWeek(dowOut)
                    .build();
        } else {
            return (T) TimeChartRowDto.builder()
                    .hours(hours).dows(dows)
                    .timeOfDay(hourOut)
                    .dayOfWeek(dowOut)
                    .build();
        }
    }

    /* ===================== Helper Methods ===================== */

    /**
     * 시간 슬롯 문자열에서 시(hour) 값을 파싱하여 반환한다.
     *
     * <p>
     * 슬롯 포맷은 {@code "HH:00-HH:59"} 형태를 가정한다.
     * - 정상값 예: "08:00-08:59" → 8
     * - 파싱 실패 또는 null 입력 시 음수(-1)를 반환하여 정렬/우선순위 로직에서 맨 아래로 배치하게 한다.
     * </p>
     *
     * <h3>사용처</h3>
     * <ul>
     *   <li>TimeRows의 그룹 정렬(시간 내림차순) 및 sumMap 조회 키로 사용</li>
     * </ul>
     *
     * @param slot "HH:00-HH:59" 형태의 시간 슬롯 문자열 (null 허용)
     * @return 파싱된 시간(0~23) 또는 파싱 실패 시 -1
     */
    private int hourFromSlot(String slot) {
        if (slot == null) return -1; // 실패 시 맨 아래로
        int idx = slot.indexOf(':');
        if (idx <= 0) return -1;
        try {
            return Integer.parseInt(slot.substring(0, idx));
        } catch (NumberFormatException ex) {
            return -1;
        }
    }
    /**
     * 정수 시(hour)를 시간 슬롯 텍스트로 포맷한다.
     *
     * <p>예: 8 → "08:00-08:59"</p>
     *
     * @param h 시간 (0~23)
     * @return {@code "HH:00-HH:59"} 형식의 문자열
     */
    private static String hourSlotText(int h) {
        return String.format("%02d:00-%02d:59", h, h); }

    /**
     * 한글 요일 문자열(예: "일","월",...)을 내부 정수(1..7)로 변환한다.
     *
     * <p>
     * 반환값은 MySQL의 DAYOFWEEK 결과와 동일한 맵핑(1=일 ... 7=토)을 사용한다.
     * 알 수 없는 값이거나 null인 경우 {@link Integer#MAX_VALUE}를 반환하여 정렬 시 맨 뒤로 보내도록 한다.
     * </p>
     *
     * @param dowKo 한글 요일(예: "일","월",...); null 허용
     * @return 요일 정수(1..7) 또는 알 수 없는 값일 경우 Integer.MAX_VALUE
     */
    private int dowIntFromKo(String dowKo) {
        if (dowKo == null) return Integer.MAX_VALUE;
        return switch (dowKo) {
            case "일" -> 1; case "월" -> 2; case "화" -> 3; case "수" -> 4;
            case "목" -> 5; case "금" -> 6; case "토" -> 7;
            default -> Integer.MAX_VALUE;
        };
    }

    /**
     * 요일 정수(1..7)를 한글 요일 문자열로 변환한다.
     *
     * @param d 요일 정수 (1=일 ... 7=토)
     * @return 한글 요일 ("일".."토"), 유효범위 밖이면 "-" 반환
     */
    private static String dowKoFromInt(int d) {
        return switch (d) {
            case 1 -> "일"; case 2 -> "월"; case 3 -> "화";
            case 4 -> "수"; case 5 -> "목"; case 6 -> "금";
            case 7 -> "토"; default -> "-";
        };
    }

    /**
     * ORDER BY NULL을 나타내는 OrderSpecifier를 반환한다.
     *
     * <p>
     * 목적: MySQL의 filesort(임시테이블/정렬)를 회피하기 위해
     * 불필요한 정렬을 의도적으로 제거할 때 사용한다(카드/단일 집계 전용).
     * </p>
     *
     * @return {@link OrderSpecifier} 인스턴스 (NULL 기준 정렬)
     */
    private OrderSpecifier<Integer> orderByNull() {
        return new OrderSpecifier<>(Order.ASC, Expressions.nullExpression());
    }


    /**
     * 모든 조회용 JPAQuery에 공통으로 적용할 힌트를 설정한다.
     *
     * <p>
     * 힌트:
     * <ul>
     *   <li>org.hibernate.readOnly = true (엔티티 스냅샷/변경감지 비용 회피)</li>
     *   <li>org.hibernate.flushMode = COMMIT (불필요한 flush 방지)</li>
     *   <li>jakarta.persistence.query.timeout = 30000 (쿼리 타임아웃 30초)</li>
     * </ul>
     * </p>
     *
     * @param q 구성된 {@link JPAQuery}
     * @param <T> 쿼리 결과 타입
     * @return 힌트가 적용된 {@link JPAQuery}
     */
    private <T> JPAQuery<T> readHints(JPAQuery<T> q) {
        return q.setHint("org.hibernate.readOnly", true)
                .setHint("org.hibernate.flushMode", "COMMIT")
                .setHint("jakarta.persistence.query.timeout", 30000);
    }

    /**
     * KPI/목록 조회의 공통 WHERE 조건을 생성한다.
     *
     * <p>
     * 포함:
     * <ul>
     *   <li>상태: COMPLETED 고정</li>
     *   <li>기간: start/end가 지정된 경우 {@link #betweenDateClosedOpen(DateTimePath, LocalDate, LocalDate)} 적용</li>
     *   <li>점포 필터: cond.getStoreIds()</li>
     * </ul>
     * 반환된 BooleanExpression은 QueryDSL where절에 안전하게 전달 가능(null 필터 제외).
     * </p>
     *
     * @param cond 사용자 조회 조건
     * @param co QCustomerOrder 인스턴스
     * @param s QStore 인스턴스
     * @return 조합된 {@link BooleanExpression} (필요시 null 포함)
     */
    private BooleanExpression eqKpiFilter(AnalyticsSearchDto cond, QCustomerOrder co, QStore s) {
        // 기본 필터: 완료 상태만 대상으로 조회
        // - 변경 감지/부분 상태 혼입 방지, KPI의 일관성 확보
        BooleanExpression where = co.status.eq(OrderStatus.COMPLETED);

        // 기간 필터: 닫힌–열린 [start, end) 규약 적용
        // - 컬럼 함수 미사용(orderedAt에 인덱스 적용 가능)
        // - start 또는 end 중 하나라도 없으면 기간 필터를 생략(요청된 그대로)
        if (cond.getStartDate() != null && cond.getEndDate() != null) {
            where = where.and(betweenDateClosedOpen(co.orderedAt, cond.getStartDate(), cond.getEndDate()));
        }

        // 점포 필터: 선택된 점포가 있을 때만 IN 조건 추가
        // - null/빈 리스트는 전체 점포로 간주(조건 생략 → WHERE에서 자동 제외)
        // - s.id IN (...) 는 대응 인덱스 활용 전제
        List<Long> storeIds = cond.getStoreIds();
        if (storeIds != null && !storeIds.isEmpty()) {
            where = where.and(s.id.in(storeIds));
        }

        // 조합된 BooleanExpression 반환
        // - 상위 쿼리에서 .where(where) 한 번만 적용 → 가독성/중복체크 비용 최소화
        return where;
    }

    /**
     * DateTimePath 컬럼에 대해 [start 00:00:00, end+1 00:00:00) 닫힌-열린 구간 조건을 반환한다.
     *
     * <p>
     * 목적: DATE/시간 함수 사용을 피하고 인덱스(orderedAt) 를 효과적으로 사용하기 위함.
     * - start, end 모두 null이면 null 반환(조건 없음)
     * - end는 inclusive로 해석되며 내부적으로 end.plusDays(1).atStartOfDay() 미만(<) 조건을 사용
     * </p>
     *
     * @param path DateTimePath (예: co.orderedAt)
     * @param start 조회 시작일 (inclusive, null 허용)
     * @param end 조회 종료일 (inclusive, null 허용)
     * @return {@link BooleanExpression} (필요시 null)
     */
    private BooleanExpression betweenDateClosedOpen(DateTimePath<LocalDateTime> path, LocalDate start, LocalDate end) {
        // 날짜 경계 정규화: [start, end) (닫힌–열린)
        // - 컬럼에 함수(DATE(), YEAR() 등) 적용 금지 → 인덱스 사용성 유지
        // - end는 '일 단위 포함' 요청을 반영하기 위해 다음날 00:00 미만(<)으로 구현
        // - null 반환 시 QueryDSL .where(...)에서 자동 제외(조건 생략 효과)
        // - start만 있으면 start 00:00 이상, end만 있으면 end+1 00:00 미만
        // - DATETIME/TIMESTAMP 소수초와 무관하게 안전(예: 23:59:59.999999 포함)
        // - 시간대: path가 로컬(Asia/Seoul) 기준이라고 가정.
        //   DB가 UTC 저장이면 start/end를 사전에 UTC로 변환해 전달하는 것이 안전.
        if (start != null && end != null) {
            return path.goe(start.atStartOfDay()).and(path.lt(end.plusDays(1).atStartOfDay()));
        } else if (start != null) {
            return path.goe(start.atStartOfDay());
        } else if (end != null) {
            return path.lt(end.plusDays(1).atStartOfDay());
        } else {
            return null; // 기간 미지정: 상위 where에서 자동 무시됨
        }
    }

    /**
     * DatePath 컬럼에 대해 [start, end+1) 닫힌-열린 구간 조건을 반환한다.
     *
     * @param path DatePath (예: DATE 컬럼)
     * @param start 조회 시작일 (inclusive, null 허용)
     * @param end 조회 종료일 (inclusive, null 허용)
     * @return {@link BooleanExpression} 또는 null
     */
    private BooleanExpression betweenDateClosedOpen(DatePath<LocalDate> path,
                                                    LocalDate start, LocalDate end) {
        if (start != null && end != null) {
            return path.goe(start).and(path.lt(end.plusDays(1)));
        } else if (start != null) {
            return path.goe(start);
        } else if (end != null) {
            return path.lt(end.plusDays(1));
        } else {
            return null;
        }
    }

    /**
     * MariaDB DATE_FORMAT 호출을 감싸는 표현식을 생성한다.
     *
     * <p>예: dateFormat(co.orderedAt, "%Y-%m")</p>
     *
     * @param dateTime DATE/TIMESTAMP 컬럼 또는 표현식
     * @param fmt DATE_FORMAT 포맷 문자열 (예: "%Y-%m-%d")
     * @return {@link StringExpression} (DATE_FORMAT 표현)
     */
    private StringExpression dateFormat(Expression<?> dateTime, String fmt) {
        return Expressions.stringTemplate("DATE_FORMAT({0}, {1})", dateTime, Expressions.constant(fmt));
    }

    /**
     * 조건이 참일 때만 합계 값을 더하는 SUM(CASE WHEN ...) 표현을 생성한다.
     *
     * <p>
     * 반환되는 표현식은 QueryDSL에서 바로 {@code .sum()}과 동등한 효과를 내며,
     * 여러 기간을 동시에 한 쿼리에서 계산할 때 사용된다.
     * </p>
     *
     * @param cond 조건식
     * @param val 합계 대상 숫자 표현식
     * @return {@link NumberExpression}{@code <BigDecimal>} (SUM CASE 표현)
     */
    private NumberExpression<BigDecimal> sumIf(BooleanExpression cond, NumberExpression<BigDecimal> val) {
        // 조건부 합계: SUM(CASE WHEN cond THEN val ELSE 0 END)
        // - 단일 스캔 내 다중 지표 계산(여러 sumIf들을 한 번에) → 스캔 수/왕복 수 줄임
        // - WHERE로 거르지 않고 CASE로 분기하므로, 다른 지표의 母집합이 동일하게 유지됨
        // - 타입 안정성: ELSE 0을 BigDecimal 상수로 명시해 합계 타입을 BigDecimal로 고정
        // - 인덱스 영향 없음: 조건판단은 스캔된 로우에 대해 수행
        // - 참고: val이 NULL 가능하면 THEN NULL이 되어 합계에 반영되지 않음(표준 SUM은 NULL 무시).
        //   반드시 0으로 처리해야 한다면, val을 COALESCE로 감싸는 대안:
        //   Expressions.numberTemplate(BigDecimal.class, "COALESCE({0},0)", val)
        return new CaseBuilder()
                .when(cond).then(val)
                .otherwise(Expressions.constant(BigDecimal.ZERO))
                .sum();
    }


    /**
     * {@code SUM(CASE WHEN cond THEN 1 ELSE 0 END)} 표현을 생성한다.
     *
     * @param cond 조건식
     * @return {@link NumberExpression}{@code <Long>}
     */
    private NumberExpression<Long> countIf(BooleanExpression cond) {
        return Expressions.numberTemplate(Long.class, "SUM(CASE WHEN {0} THEN 1 ELSE 0 END)", cond);
    }

    /**
     * 분모가 0이거나 null인 경우 0을 반환하여 0-나눗셈 예외를 방지한다.
     *
     * @param num 분자
     * @param den 분모
     * @param scale 결과 소수점 자리수
     * @return 나눗셈 결과 또는 0
     */
    private static BigDecimal divOrZero(BigDecimal num, BigDecimal den, int scale) {
        return (den == null || den.signum() == 0) ? BigDecimal.ZERO : num.divide(den, scale, RoundingMode.HALF_UP);
    }

    /**
     * BigDecimal null 안전 처리(nvl)
     *
     * @param v BigDecimal (null 허용)
     * @return null이면 BigDecimal.ZERO, 아니면 원값
     */
    private static BigDecimal nz(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }

    /**
     * Long null 안전 처리
     *
     * @param v Long (null 허용)
     * @return null이면 0L, 아니면 원값
     */
    private static Long       nz(Long v)       { return v == null ? 0L : v; }

    /**
     * Integer null 안전 처리
     *
     * @param v Integer (null 허용)
     * @return null이면 0, 아니면 원값
     */
    @SuppressWarnings("unused")
    private static Integer    nz(Integer v)    { return v == null ? 0 : v; }

    /**
     * 조회 기간을 사람이 읽기 쉬운 라벨 형태로 생성한다.
     *
     * @param start 시작일 (null 허용)
     * @param end 종료일 (null 허용)
     * @return 예: "2025-01-01 ~ 2025-10-31", start만/ end만일 경우에도 적절히 표시
     */
    private String rangeLabel(LocalDate start, LocalDate end) {
        if (start == null && end == null) return "";
        if (start == null) return "~ " + end.toString();
        if (end == null) return start.toString() + " ~";
        return start.toString() + " ~ " + end.toString();
    }

    /**
     * 성능 로깅(timing)용 래퍼: 함수 실행 전후 시간 로깅을 수행한다.
     *
     * <p>로그 포맷: {@code [PERF][Repo] <tag>: <millis> ms}</p>
     *
     * @param tag 식별 태그
     * @param fn 실행할 Supplier 람다
     * @param <T> 결과 타입
     * @return Supplier가 반환한 값
     */
    private <T> T timed(String tag, Supplier<T> fn) {
        long st = System.currentTimeMillis();
        try { return fn.get(); }
        finally { log.info("[PERF][Repo] {}: {} ms", tag, System.currentTimeMillis() - st); }
    }

    /**
     * 성능 로깅(timing)용 래퍼(무반환 버전).
     *
     * @param tag 식별 태그
     * @param fn 실행할 Runnable
     */
    private void timedRun(String tag, Runnable fn) {
        long st = System.currentTimeMillis();
        try { fn.run(); }
        finally { log.info("[PERF][Repo] {}: {} ms", tag, System.currentTimeMillis() - st); }
    }

    /**
     * Comp 결과(점포 단위 또는 전체)를 담는 내부 구조체.
     *
     * <p>값은 퍼센트(예: +5.23) 형태로 보관된다(소수점 표시는 호출부에서 포맷).</p>
     */
    private static class GlobalComp {
        // 널 방지용 상수 객체
        // - 비교값이 없을 때(데이터 공백/분모 0 등) null 대신 ZERO를 반환하여
        //   호출부에서의 null-check 분기를 제거한다.
        static final GlobalComp ZERO = new GlobalComp(BigDecimal.ZERO, BigDecimal.ZERO);

        // 불변 비교 지표(퍼센트 값)
        // - 단위: % (예: 12.3% → 12.3)
        // - 스케일/반올림은 호출부에서 setScale(1, HALF_UP) 등으로 통일한다.
        // - BigDecimal은 불변이므로 스레드-세이프하게 공유 가능.
        final BigDecimal compMoM; // 전월 대비 증감률(%)
        final BigDecimal compYoY; // 전년 대비 증감률(%)

        // 생성자: 불변 객체로 사용(필드 final)
        // - 값 범위는 음수/양수 모두 가능(감소/증가)
        // - 필요한 경우 정규화/반올림은 외부에서 수행
        GlobalComp(BigDecimal mom, BigDecimal yoy) {
            this.compMoM = mom;
            this.compYoY = yoy;
        }
    }


    /**
     * 단일 점포 선택(또는 조건 필터 적용 시)의 MoM/YoY 비교값을 계산한다.
     *
     * <p>
     * 계산 방식:
     * <ul>
     *   <li>A/B 비교: calcMoMWindows()로 산출한 A(현재 윈도우), B(비교 윈도우)를 한 번의 넓은 스캔(lytdStart ~ ytdEnd)으로 가져와 SUM(CASE)로 계산</li>
     *   <li>YoY: YTD vs LYTD를 동일 방식으로 계산</li>
     * </ul>
     * 이 메서드는 DB 스캔 범위를 넓게 잡아 필요한 모든 기간별 합계를 한 번에 가져오도록 설계되어 있다.
     * </p>
     *
     * @param cond 조회 조건 (점포 필터 포함 가능)
     * @return {@link GlobalComp} (MoM, YoY 값; 값이 없을 경우 0)
     */
    private GlobalComp computeGlobalComp(AnalyticsSearchDto cond) {
        // 0) 기준일 및 비교 윈도우 계산
        //    - today: Asia/Seoul
        //    - MoM: (aStart~aEnd) vs (bStart~bEnd)
        //    - YoY: YTD(당해 1/1~어제) vs LYTD(전년 동기간)
        final var today     = LocalDate.now(ZONE_SEOUL);

        MoMWindows w = calcMoMWindows(today);
        final var aStart = w.aStart();
        final var aEnd   = w.aEnd();
        final var bStart = w.bStart();
        final var bEnd   = w.bEnd();

        final var ytdStart  = LocalDate.of(today.getYear(), 1, 1);
        final var ytdEnd    = today.minusDays(1);
        final var lytdStart = ytdStart.minusYears(1);
        final var lytdEnd   = ytdEnd.minusYears(1);

        // 1) 공통 WHERE(스캔 범위): [lytdStart, ytdEnd)
        //    - 한 번의 스캔으로 A/B/YTD/LYTD를 모두 sumIf 분기로 계산하기 위함
        //    - 상태 완료 + (선택 시) 점포 IN + 기간 범위
        //    - 점포 필터는 null/empty면 자동 제외
        BooleanExpression base =
                co.status.eq(OrderStatus.COMPLETED)
                        .and((cond.getStoreIds()!=null && !cond.getStoreIds().isEmpty()) ? s.id.in(cond.getStoreIds()) : null)
                        .and(betweenDateClosedOpen(co.orderedAt, lytdStart, ytdEnd)); // 스캔 범위(닫힌–열린)

        // 2) 단일 쿼리로 MoM/YoY에 필요한 합계 동시 계산
        //    - A/B : MoM 비교구간
        //    - Y/LY: YTD/LYTD
        //    - 정렬 불필요(단일 로우 집계) → ORDER BY 생략
        Tuple t = readHints(
                query.select(
                                // A / B (MoM)
                                sumIf(betweenDateClosedOpen(co.orderedAt, aStart, aEnd), co.totalPrice),   // idx 0 = A
                                sumIf(betweenDateClosedOpen(co.orderedAt, bStart, bEnd), co.totalPrice),   // idx 1 = B
                                // YTD / LYTD (YoY)
                                sumIf(betweenDateClosedOpen(co.orderedAt, ytdStart,  ytdEnd),  co.totalPrice), // idx 2 = Y
                                sumIf(betweenDateClosedOpen(co.orderedAt, lytdStart, lytdEnd), co.totalPrice)  // idx 3 = LY
                        )
                        .from(co).join(co.store, s) // 명시적 조인(점포 필터/멀티테넌시 대비)
                        .where(base)
        ).fetchOne();

        // 3) 널 방어 및 기본값
        BigDecimal a  = t != null ? nz(t.get(0, BigDecimal.class)) : BigDecimal.ZERO;
        BigDecimal b  = t != null ? nz(t.get(1, BigDecimal.class)) : BigDecimal.ZERO;
        BigDecimal y  = t != null ? nz(t.get(2, BigDecimal.class)) : BigDecimal.ZERO;
        BigDecimal ly = t != null ? nz(t.get(3, BigDecimal.class)) : BigDecimal.ZERO;

        // 4) 비교 지표 계산
        //    - MoM = (A/B - 1) × 100
        //    - YoY = (Y/LY - 1) × 100
        //    - 분모 0 보호, 스케일/반올림은 호출부에서 setScale로 통일(설계 원칙)
        BigDecimal mom = (b.signum()==0)
                ? BigDecimal.ZERO
                : a.divide(b, 6, RoundingMode.HALF_UP).subtract(BigDecimal.ONE).multiply(BigDecimal.valueOf(100));
        BigDecimal yoy = (ly.signum()==0)
                ? BigDecimal.ZERO
                : y.divide(ly, 6, RoundingMode.HALF_UP).subtract(BigDecimal.ONE).multiply(BigDecimal.valueOf(100));

        // 5) 불변 DTO-like 값 객체 반환
        //    - null 대신 ZERO 사용을 권장(GlobalComp.ZERO)하나, 여기서는 실측 값 반환
        return new GlobalComp(mom, yoy);
    }


    /**
     * 복수 점포(또는 전사) 선택 시 점포별 Comp(MoM/YoY)를 계산하여 맵으로 반환한다.
     *
     * <p>
     * - 입력: sidsOnPage — 페이지에 표시되는 점포 ID 집합 (계산 범위를 이로 제한)
     * - 출력: 점포 ID → {@link GlobalComp}
     * </p>
     *
     * <h3>성능 노트</h3>
     * <ul>
     *   <li>단일 그룹쿼리로 각 점포의 A/B/Y/LY 합계를 동시에 계산(집계 CASE 활용)</li>
     *   <li>필요에 따라 병렬 처리(점포 수가 많을 경우)로 확장 가능</li>
     * </ul>
     *
     * @param sidsOnPage 페이지에 표시된 점포 ID 집합 (null/빈 경우 빈 맵 반환)
     * @return 점포별 {@link GlobalComp} 맵
     */
    private Map<Long, GlobalComp> computeCompByStore(Set<Long> sidsOnPage) {
        // 0) 빠른 탈출: 현재 페이지에 표시된 점포가 없으면 빈 맵 반환
        // - 불필요한 스캔/집계 방지
        Map<Long, GlobalComp> out = new HashMap<>();
        if (sidsOnPage == null || sidsOnPage.isEmpty()) return out;

        // 1) 기준일 및 비교 윈도우 산출(Asia/Seoul)
        //    - MoM: (aStart~aEnd) vs (bStart~bEnd)
        //    - YoY: YTD(당해 1/1~어제) vs LYTD(전년 동기간)
        final var today = LocalDate.now(ZONE_SEOUL);

        MoMWindows w = calcMoMWindows(today);
        final var aStart = w.aStart();
        final var aEnd   = w.aEnd();
        final var bStart = w.bStart();
        final var bEnd   = w.bEnd();

        final var ytdStart  = LocalDate.of(today.getYear(), 1, 1);
        final var ytdEnd    = today.minusDays(1);
        final var lytdStart = ytdStart.minusYears(1);
        final var lytdEnd   = ytdEnd.minusYears(1);

        // 2) 단일 그룹 집계 쿼리: 점포별(s.id)로 A/B/Y/LY 합계를 동시 계산
        //    - WHERE 스캔 범위는 [lytdStart, ytdEnd)로 넓게 설정하고,
        //      sumIf(between...) 분기로 각 기간의 합계를 한 번에 계산 → 스캔 1회
        //    - 상태 완료 + 현재 페이지 점포만(s.id IN ...)으로 범위 축소
        //    - 정렬 불필요(그룹 집계만) → ORDER BY 생략
        List<Tuple> rows = readHints(
                query.select(
                                s.id,
                                // A / B (MoM)
                                sumIf(betweenDateClosedOpen(co.orderedAt, aStart, aEnd), co.totalPrice),
                                sumIf(betweenDateClosedOpen(co.orderedAt, bStart, bEnd), co.totalPrice),
                                // YTD / LYTD (YoY)
                                sumIf(betweenDateClosedOpen(co.orderedAt, ytdStart,  ytdEnd),  co.totalPrice),
                                sumIf(betweenDateClosedOpen(co.orderedAt, lytdStart, lytdEnd), co.totalPrice)
                        )
                        .from(co).join(co.store, s) // 명시적 조인(크로스 조인 방지)
                        .where(
                                co.status.eq(OrderStatus.COMPLETED),
                                s.id.in(sidsOnPage),                                   // 현재 페이지 점포만 대상
                                betweenDateClosedOpen(co.orderedAt, lytdStart, ytdEnd) // 스캔 범위(닫힌–열린)
                        )
                        .groupBy(s.id)
        ).fetch();

        // 3) 각 점포별로 MoM/YoY 계산
        //    - MoM = (A/B - 1) × 100, YoY = (Y/LY - 1) × 100
        //    - 분모 0 보호: 0이면 0%로 처리
        for (Tuple t : rows) {
            Long sid      = t.get(0, Long.class);
            BigDecimal a  = nz(t.get(1, BigDecimal.class));
            BigDecimal b  = nz(t.get(2, BigDecimal.class));
            BigDecimal y  = nz(t.get(3, BigDecimal.class));
            BigDecimal ly = nz(t.get(4, BigDecimal.class));

            BigDecimal mom = (b.signum()==0)
                    ? BigDecimal.ZERO
                    : a.divide(b, 6, RoundingMode.HALF_UP).subtract(BigDecimal.ONE).multiply(BigDecimal.valueOf(100));

            BigDecimal yoy = (ly.signum()==0)
                    ? BigDecimal.ZERO
                    : y.divide(ly, 6, RoundingMode.HALF_UP).subtract(BigDecimal.ONE).multiply(BigDecimal.valueOf(100));

            out.put(sid, new GlobalComp(mom, yoy));
        }

        // 4) 점포별 비교 지표 맵 반환
        return out;
    }


    /* ===================== MoM windows / Counts ===================== */

    /**
     * MoM 비교용 기간 윈도우: A (비교 대상), B (이전 비교 대상) 를 담는 레코드.
     *
     * <p>
     * semantics:
     * <ul>
     *   <li>aStart..aEnd — 비교할 현재 윈도우 (예: MTD)</li>
     *   <li>bStart..bEnd — 비교 대상 윈도우 (예: PMT)</li>
     * </ul>
     * </p>
     */
    private record MoMWindows(LocalDate aStart, LocalDate aEnd, LocalDate bStart, LocalDate bEnd) {}

    /**
     * MoM (Month-over-Month) 윈도우를 계산한다.
     *
     * <p>
     * 규칙:
     * <ul>
     *   <li>매월 1일: A = 전월 전체, B = 전전월 전체</li>
     *   <li>그 외: A = 이번달 1일 ~ 어제, B = 지난달 1일 ~ A와 동일 일수 (월말 캡 처리)</li>
     * </ul>
     * </p>
     *
     * @param today 기준일 (보통 LocalDate.now(ZoneId))
     * @return {@link MoMWindows} (A/B 기간)
     */
    private MoMWindows calcMoMWindows(LocalDate today) {
        // MoM 비교(A/B) 구간 산출
        // - 본 프로젝트의 기간 필터는 [start, end) (닫힌–열린) 규약이므로
        //   여기서 반환하는 end는 "포함되는 마지막 일자"로 설정한다.
        //   즉, betweenDateClosedOpen(..., start, end)에서 end+1일까지 '<' 비교로 처리됨.

        if (today.getDayOfMonth() == 1) {
            // ⓐ 매월 1일인 경우
            //  - A: 전월 전체
            //  - B: 전전월 전체
            //  - 당월 데이터가 아직 0일이므로 “부분월 vs 전월” 비교 대신 “완전월 vs 완전월” 비교
            LocalDate prevStart  = today.minusMonths(1).withDayOfMonth(1);
            LocalDate prevEnd    = prevStart.plusMonths(1).minusDays(1);   // 전월 말일(포함)
            LocalDate pprevStart = today.minusMonths(2).withDayOfMonth(1);
            LocalDate pprevEnd   = pprevStart.plusMonths(1).minusDays(1);  // 전전월 말일(포함)
            return new MoMWindows(prevStart, prevEnd, pprevStart, pprevEnd);

        } else {
            // ⓑ 그 외(2~말일)
            //  - A: 당월 1일 ~ 오늘-1일 (부분월)
            //  - B: 전월 동일 일수 구간(월 말 초과 시 월 말로 캡)
            //    예) 오늘이 9/10이면 A=9/1~9/9(9일), B=8/1~8/9(9일)
            //        오늘이 3/31이면 A=3/1~3/30(30일), B=2/1~2/28 or 29(캡 처리)
            LocalDate aStart = today.withDayOfMonth(1);
            LocalDate aEnd   = today.minusDays(1); // 포함 마지막 일자
            long dayCount = Math.max(1, java.time.temporal.ChronoUnit.DAYS.between(aStart, aEnd) + 1);

            // 전월 시작일과 B 구간 산출
            LocalDate bStart = aStart.minusMonths(1);
            LocalDate bEnd   = bStart.plusDays(dayCount - 1);           // 전월에서 동일 일수만큼
            LocalDate bLast  = bStart.plusMonths(1).minusDays(1);       // 전월 말일
            if (bEnd.isAfter(bLast)) bEnd = bLast;                      // 말일 초과 시 캡(윤년/월길 보정)

            return new MoMWindows(aStart, aEnd, bStart, bEnd);
        }
    }


    /**
     * KPI 라벨(점포 × 라벨[일/월]) 기준의 전체 행 수를 계산한다.
     *
     * <p>
     * 이 메서드는 페이징 처리 시 전체 행 수(total)를 계산하기 위해 사용된다.
     * 라벨 포맷은 cond.getViewBy()에 따라 월별("%Y-%m") 또는 일별("%Y-%m-%d")로 결정되며,
     * 점포 ID와 라벨을 결합한 문자열을 DISTINCT COUNT 하여 per-store 라인 수를 산출한다.
     * </p>
     *
     * <h3>중요 구현 노트</h3>
     * <ul>
     *   <li>COUNT는 {@code COUNT(DISTINCT CONCAT_WS('|', store_id, label))} 패턴을 사용하여 DB에서 중복을 제거함.</li>
     *   <li>조회조건은 {@link #eqKpiFilter(AnalyticsSearchDto, QCustomerOrder, QStore)}를 통해 생성된 baseFilter를 사용.</li>
     *   <li>읽기 힌트({@link #readHints(JPAQuery)})를 적용하여 읽기 전용/타임아웃 등 안전장치 적용.</li>
     * </ul>
     *
     * @param cond 조회 조건(기간, 점포목록, viewBy 등). null 허용은 아니나 내부에서 null 체크는 수행함.
     * @return 조건에 맞는 KPI 라인(점포×라벨) 전체 수 (페이지네이션을 위한 total)
     */
    @Override
    public long countKpi(AnalyticsSearchDto cond) {
        // 0) 라벨 단위 선택: 월/일
        // - labelExpr은 표시/그룹 구분용 포맷 문자열(%Y-%m 또는 %Y-%m-%d)
        // - 성능 메모: labelExpr는 DATE_FORMAT이지만 WHERE에 사용하지 않고
        //   COUNT(DISTINCT) 키 생성에만 쓰므로 인덱스 사용성에 영향 없음(필터는 baseFilter가 담당).
        boolean byMonth = cond.getViewBy() == ViewBy.MONTH;
        String fmt = byMonth ? "%Y-%m" : "%Y-%m-%d";
        StringExpression labelExpr = dateFormat(co.orderedAt, fmt);

        // 1) 공통 KPI 필터
        // - 완료 상태 + 기간 [start, end) + (선택) 점포 IN
        // - 컬럼 함수 미사용 원칙 준수 → 날짜 인덱스 활용 가능
        BooleanExpression baseFilter = eqKpiFilter(cond, co, s);

        // 2) DISTINCT 카운트를 위한 그룹 키
        // - (store_id, label) 조합의 고유 버킷 개수 = 페이지 total
        // - JPA/MariaDB에서 다중 컬럼 DISTINCT 카운트를 간편히 만들기 위해
        //   문자열 키(CONCAT_WS)로 합친 뒤 COUNT(DISTINCT ...) 수행
        // - 구분자 '|'는 labelExpr(숫자/하이픈)과 충돌 위험이 사실상 없음
        StringExpression groupKey = Expressions.stringTemplate(
                "CONCAT_WS('|',{0},{1})", s.id, labelExpr
        );

        // 3) 카운트 쿼리
        // - 불필요 정렬/그룹 결과 반환 없음 → ORDER BY 생략
        // - 힌트(readOnly/flush/timeout)는 readHints(...)에서 일괄 적용
        return Optional.ofNullable(
                readHints(
                        query.select(Expressions.numberTemplate(Long.class, "COUNT(DISTINCT {0})", groupKey))
                                .from(co).join(co.store, s)
                                .where(baseFilter)
                ).fetchOne()
        ).orElse(0L); // null 방어(결과 없을 때 0)
    }


    /**
     * 주문 목록(Orders) 조회용 전체 행 수를 계산한다.
     *
     * <p>
     * - 월별 모드(byMonth=true)일 경우: 점포 × 연 × 월 단위의 distinct 카운트를 반환한다.
     *   (COUNT DISTINCT CONCAT_WS('|', store_id, YEAR(orderedAt), MONTH(orderedAt)))
     * - 일별 모드일 경우: 점포 × 메뉴 × 주문일(원본 orderedAt) × 주문유형(OrderType) 단위로 distinct 카운트를 반환한다.
     *   (이는 findOrders()의 그룹 기준과 1:1 대응하여 페이징 기준과 일관성을 유지하기 위함)
     * </p>
     *
     * <h3>성능·정확성 노트</h3>
     * <ul>
     *   <li>가능한 한 DB에서 중복제거를 수행하여 애플리케이션 레벨에서의 후처리 비용을 줄임.</li>
     *   <li>기간/점포 필터는 where 절에 적용되어 불필요한 스캔을 제한.</li>
     *   <li>조회에 사용되는 date 함수(YEAR/MONTH)는 count 기준(라벨 생성) 용도로만 사용되며, 큰 스캔 범위에서는 인덱스 영향에 유의.</li>
     * </ul>
     *
     * @param cond 조회 조건 (viewBy, 기간, 점포 등). viewBy가 null이면 일별(false)로 처리.
     * @return Orders 목록을 페이지네이션 하기 위한 전체 행 수
     */
    @Override
    public long countOrders(AnalyticsSearchDto cond) {
        // 0) 라벨 단위 선택(월/일)
        // - cond.getViewBy() == MONTH → 월별 카운트, 그 외/NULL → 일별(상세) 카운트
        boolean byMonth = (cond.getViewBy() == ViewBy.MONTH);
        if (cond.getViewBy() == null) {
            byMonth = false; // 기본값: 일별
        }

        // 0-1) 월별 키 생성을 위한 파생식(YEAR/MONTH)
        // - 성능 메모: YEAR()/MONTH()는 WHERE에 사용되지 않고 DISTINCT 키 생성에만 사용됨
        //   → 날짜 인덱스 사용성에 영향 없음
        NumberExpression<Integer> yExpr = Expressions.numberTemplate(Integer.class, "YEAR({0})",  co.orderedAt);
        NumberExpression<Integer> mExpr = Expressions.numberTemplate(Integer.class, "MONTH({0})", co.orderedAt);

        // 1) 공통 WHERE
        // - 상태 완료 + 기간 [start, end) + (선택) 점포 IN
        BooleanExpression filter = co.status.eq(OrderStatus.COMPLETED);
        if (cond.getStartDate() != null || cond.getEndDate() != null) {
            filter = filter.and(betweenDateClosedOpen(co.orderedAt, cond.getStartDate(), cond.getEndDate()));
        }
        if (cond.getStoreIds() != null && !cond.getStoreIds().isEmpty()) {
            filter = filter.and(s.id.in(cond.getStoreIds()));
        }

        // 2) COUNT DISTINCT
        // - 월별: (storeId | YEAR | MONTH)의 고유 조합 수
        // - 일별: (storeId | menuId | orderedAt | orderType)의 고유 조합 수
        //   * 일별은 "메뉴 단위" 테이블을 세는 것이므로 menuId 포함
        //   * 정렬 불필요 → ORDER BY 생략(집계 전용)
        //   * CONCAT_WS('|', ...)는 다중 컬럼 DISTINCT를 간단히 만들기 위한 문자열 키
        //     - 구분자 '|'는 숫자/하이픈 기반 라벨과 충돌 가능성이 사실상 없음
        return Optional.ofNullable(
                readHints(
                        byMonth
                                // 월별: co + s만 조인 (매출/거래 집계 키와 동일)
                                ? query.select(Expressions.numberTemplate(Long.class,
                                        "COUNT(DISTINCT CONCAT_WS('|', {0}, {1}, {2}))", s.id, yExpr, mExpr))
                                .from(co).join(co.store, s)
                                .where(filter)

                                // 일별: 메뉴 단위 키이므로 cod + m 조인 필요
                                : query.select(Expressions.numberTemplate(Long.class,
                                        "COUNT(DISTINCT CONCAT_WS('|', {0}, {1}, {2}, {3}))",
                                        s.id, m.menuId, co.orderedAt, co.orderType))
                                .from(co)
                                .join(cod).on(cod.order.eq(co))
                                .join(co.store, s)
                                .join(cod.menuIdFk, m)
                                .where(filter)
                ).fetchOne()
        ).orElse(0L); // null 방어
    }


    /**
     * 시간·요일 분석(시간대/요일) 테이블의 전체 행 수를 계산한다.
     *
     * <p>
     * 이 카운트는 {@link #findTimeRows(AnalyticsSearchDto, Pageable)}의 그룹 기준과 1:1로 대응해야 한다:
     * <ul>
     *   <li>월별(byMonth=true): co 테이블 기반 그룹 (s.id, YEAR(orderedAt), MONTH(orderedAt), HOUR, DAYOFWEEK, orderType)</li>
     *   <li>일별(byMonth=false): cod 테이블 기반 그룹 (s.id, menu_id, orderedAt(original timestamp), orderType)</li>
     * </ul>
     * 일별 모드의 경우 cod를 사용하여 '메뉴 단위' 상세 행수를 정확히 계산한다.
     * </p>
     *
     * <h3>추가 노트</h3>
     * <ul>
     *   <li>월별 카운트는 08~22시(시간대 제한)를 적용하여 불필요한 시간대 스캔을 줄임.</li>
     *   <li>모든 쿼리에 대해 {@link #readHints(JPAQuery)}를 적용하여 안정성(타임아웃/읽기전용) 확보.</li>
     * </ul>
     *
     * @param cond 조회 조건(기간, 점포, viewBy 등)
     * @return findTimeRows와 동일한 그룹 기준으로 계산된 전체 행 수 (페이지네이션용)
     */
    @Override
    @Transactional(readOnly = true)
    public long countTime(AnalyticsSearchDto cond) {
        // 공통 WHERE: 완료 상태 + 기간 [start, end) (+ 선택 점포 IN)
        // - 기간 조건은 컬럼함수 미사용 → 날짜 인덱스(range scan) 활용 가능
        BooleanExpression filter = co.status.eq(OrderStatus.COMPLETED)
                .and(betweenDateClosedOpen(co.orderedAt, cond.getStartDate(), cond.getEndDate()));
        if (cond.getStoreIds() != null && !cond.getStoreIds().isEmpty()) {
            filter = filter.and(s.id.in(cond.getStoreIds()));
        }

        boolean byMonth = (cond.getViewBy() == ViewBy.MONTH);

        // 그룹/정렬용 파생식(원본 컬럼 기반)
        // - 주의: YEAR/MONTH/HOUR/DAYOFWEEK는 WHERE가 아니라 GROUP/키 생성에만 사용
        //   (기간 필터는 원본 orderedAt으로 인덱스 적용 → 파생은 스캔 후 계산)
        NumberExpression<Integer> Y = Expressions.numberTemplate(Integer.class, "YEAR({0})",  co.orderedAt);
        NumberExpression<Integer> M = Expressions.numberTemplate(Integer.class, "MONTH({0})", co.orderedAt);
        NumberExpression<Integer> H = Expressions.numberTemplate(Integer.class, "HOUR({0})",  co.orderedAt);
        NumberExpression<Integer> D = Expressions.numberTemplate(Integer.class, "DAYOFWEEK({0})", co.orderedAt);

        // findTimeRows()의 GROUP/키와 1:1 대응
        if (byMonth) {
            // 월별 테이블:
            // - from(co) 집계
            // - 그룹 키 = (storeId, Y, M, H, D, orderType)
            // - 08~22시만 카운트 (HOUR() 조건은 기간 인덱스 스캔 이후 필터로 적용)
            return Optional.ofNullable(
                    readHints(
                            query.select(Expressions.numberTemplate(Long.class,
                                            "COUNT(DISTINCT CONCAT_WS('|',{0},{1},{2},{3},{4},{5}))",
                                            s.id, Y, M, H, D, co.orderType))
                                    .from(co)
                                    .join(co.store, s)
                                    .where(filter, H.between(8, 22)) // 시간대 슬라이싱
                    ).fetchOne()
            ).orElse(0L);
        } else {
            // 일별 테이블:
            // - from(cod) 상세(메뉴 라인) 기준
            // - 그룹 키 = (storeId, menuId, orderedAt(원본), orderType)
            //   * hour/day 라벨은 orderedAt의 함수값이라 키에 중복 포함 불필요
            return Optional.ofNullable(
                    readHints(
                            query.select(Expressions.numberTemplate(Long.class,
                                            "COUNT(DISTINCT CONCAT_WS('|',{0},{1},{2},{3}))",
                                            s.id, m.menuId, co.orderedAt, co.orderType))
                                    .from(co)
                                    .join(cod).on(cod.order.eq(co))
                                    .join(co.store, s)
                                    .join(cod.menuIdFk, m)
                                    .where(filter)
                    ).fetchOne()
            ).orElse(0L);
        }
    }

	/**
	 * 재료 목록(Materials) 조회용 전체 행 수를 계산한다.
	 *
	 * <p>
	 * {@link #findMaterials(AnalyticsSearchDto, Pageable)}에서 사용하는
	 * 그룹 기준과 동일하게, 뷰 모드에 따라 다음 조합을 하나의 라인으로 본다:
	 * <ul>
	 *   <li>DAY 모드: (orderDate, storeId, materialId, purchaseOrderId)</li>
	 *   <li>MONTH 모드: (yearMonth, storeId, materialId)</li>
	 * </ul>
	 * 이 조합을 문자열 키로 만들어 {@code COUNT(DISTINCT ...)}로 카운트한다.
	 * </p>
	 *
	 * @param cond 조회 조건(기간, 점포, viewBy 등)
	 * @return 페이징을 위한 총 행 수
	 */
    @Override
    @Transactional(readOnly = true)
    public long countMaterials(AnalyticsSearchDto cond) {
        final LocalDate start = cond.getStartDate();  // inclusive
        final LocalDate end   = cond.getEndDate();    // inclusive
        final boolean byDay = (cond.getViewBy() == null || cond.getViewBy() == ViewBy.DAY);

        // [start, end) 닫힌–열린 규약(열에 함수 X)  → 인덱스 효율 확보
        final BooleanExpression periodFilter =
                betweenDateClosedOpen(ro.actualDeliveryDate, start, end);

        // 점포 필터(없으면 생략). 스키마에 스칼라 FK가 있다면 그걸 쓰면 더 좋음(조인 회피).
        final BooleanExpression storeFilter =
                (cond.getStoreIds() == null || cond.getStoreIds().isEmpty())
                        ? null
                        : ro.store.id.in(cond.getStoreIds());

        if (byDay) {
            // [DAY] 키 = (storeId, materialId, actualDeliveryDate)
            //  - DATE() 불필요: actualDeliveryDate가 DATE 컬럼이면 원본 사용
            NumberTemplate<Long> total = Expressions.numberTemplate(
                    Long.class,
                    "COUNT(DISTINCT {0}, {1}, {2})",
                    ro.store.id,
                    rod.storeMaterial.id,
                    ro.actualDeliveryDate
            );

            Long res = readHints(
                    query.select(total)
                            .from(rod)
                            .join(rod.receiveOrder, ro)
                            .where(storeFilter, periodFilter)
                            .orderBy(orderByNull())
            ).fetchOne();

            return (res == null) ? 0L : res;

        } else {
            // [MONTH] 키 = (storeId, materialId, YEAR(actualDeliveryDate), MONTH(actualDeliveryDate))
            //  - YEAR/MONTH는 "라벨 전용" 계산(WHERE/GROUP BY에 사용 안 함) → #4 준수
            NumberExpression<Integer> Y =
                    Expressions.numberTemplate(Integer.class, "YEAR({0})", ro.actualDeliveryDate);
            NumberExpression<Integer> M =
                    Expressions.numberTemplate(Integer.class, "MONTH({0})", ro.actualDeliveryDate);

            NumberTemplate<Long> total = Expressions.numberTemplate(
                    Long.class,
                    "COUNT(DISTINCT {0}, {1}, {2}, {3})",
                    ro.store.id,
                    rod.storeMaterial.id,
                    Y, M
            );

            Long res = readHints(
                    query.select(total)
                            .from(rod)
                            .join(rod.receiveOrder, ro)
                            .where(storeFilter, periodFilter)
                            .orderBy(orderByNull())
            ).fetchOne();

            return (res == null) ? 0L : res;
        }
    }




}
