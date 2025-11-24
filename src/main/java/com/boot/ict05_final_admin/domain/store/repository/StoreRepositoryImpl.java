package com.boot.ict05_final_admin.domain.store.repository;

import com.boot.ict05_final_admin.domain.auth.entity.QMember;
import com.boot.ict05_final_admin.domain.staffresources.entity.QStaffProfile;
import com.boot.ict05_final_admin.domain.staffresources.entity.StaffDepartment;
import com.boot.ict05_final_admin.domain.staffresources.entity.StaffEmploymentType;
import com.boot.ict05_final_admin.domain.store.dto.*;
import com.boot.ict05_final_admin.domain.store.entity.QStore;
import com.boot.ict05_final_admin.domain.store.entity.StoreStatus;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * {@link StoreRepositoryCustom} 구현체.
 *
 * <p>
 * Querydsl을 이용해 동적 쿼리, 프로젝션, 집계 쿼리 등을 수행한다.<br>
 * 복잡한 검색/통계 로직은 여기서 처리하고, 서비스/컨트롤러에서는 DTO만 사용한다.
 * </p>
 */
@Repository
@RequiredArgsConstructor
public class StoreRepositoryImpl implements StoreRepositoryCustom {

    /** Querydsl JPAQueryFactory (생성자 주입) */
    private final JPAQueryFactory queryFactory;

    /**
     * 검색 조건과 페이징 정보를 이용해 매장 목록을 조회한다.
     *
     * <p>
     * - {@link StoreListDTO} 프로젝션으로 필요한 필드만 조회<br>
     * - 점주명은 {@link QStaffProfile} 서브쿼리를 사용하여 OWNER 1명을 가져온다.
     * </p>
     *
     * @param storeSearchDTO 검색 조건(키워드, 상태 등)
     * @param pageable       페이징 정보
     * @return               매장 목록 페이지
     */
    @Override
    public Page<StoreListDTO> listStore(StoreSearchDTO storeSearchDTO, Pageable pageable) {
        QStore store = QStore.store;
        QStaffProfile staffProfile = QStaffProfile.staffProfile;

        List<StoreListDTO> content = queryFactory
                .select(Projections.fields(StoreListDTO.class,
                        store.id.as("storeId"),
                        store.name.as("storeName"),
                        store.status.as("storeStatus"),
                        ExpressionUtils.as(
                                JPAExpressions.select(staffProfile.staffName)
                                        .from(staffProfile)
                                        .where(
                                                staffProfile.store.id.eq(store.id),
                                                staffProfile.staffEmploymentType.eq(StaffEmploymentType.OWNER)
                                        )
                                        .limit(1),
                                "staffName"),
                        store.phone.as("storePhone"),
                        store.monthlySales.as("storeMonthlySales"),
                        store.totalEmployees.as("storeTotalEmployees")
                ))
                .from(store)
                .where(
                        eqSearchStore(storeSearchDTO, store),
                        eqStatus(storeSearchDTO, store)
                )
                .orderBy(store.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(store.count())
                .from(store)
                .where(
                        eqSearchStore(storeSearchDTO, store),
                        eqStatus(storeSearchDTO, store) // 목록과 동일 WHERE
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0L : total);
    }

    /**
     * 키워드 검색 조건을 생성한다.
     *
     * <p>
     * - id 또는 name 에 대해 부분 일치 검색<br>
     * - null/공백이면 조건을 적용하지 않는다(null 반환)
     * </p>
     *
     * @param dto   검색 조건 DTO
     * @param store QStore 엔티티 Q타입
     * @return      BooleanExpression 또는 null
     */
    private BooleanExpression eqSearchStore(StoreSearchDTO dto, QStore store) {
        String keyword = dto.getKeyword();
        if (keyword == null || keyword.isBlank()) return null;
        keyword = keyword.trim();
        return store.id.stringValue().containsIgnoreCase(keyword)
                .or(store.name.containsIgnoreCase(keyword));
    }

    /**
     * 상태 필터 검색 조건을 생성한다.
     *
     * @param dto   검색 조건 DTO
     * @param store QStore 엔티티 Q타입
     * @return      상태가 지정된 경우 조건, 아니면 null
     */
    private BooleanExpression eqStatus(StoreSearchDTO dto, QStore store) {
        return dto.getStatus() != null ? store.status.eq(dto.getStatus()) : null;
    }

    /**
     * 검색 조건에 해당하는 전체 매장 수를 반환한다.
     *
     * @param storeSearchDTO 검색 조건
     * @return               총 개수
     */
    @Override
    public long countStore(StoreSearchDTO storeSearchDTO) {
        QStore store = QStore.store;
        Long total = queryFactory
                .select(store.count())
                .from(store)
                .where(
                        eqSearchStore(storeSearchDTO, store),
                        eqStatus(storeSearchDTO, store) // ✅ 동일 WHERE 적용
                )
                .fetchOne();
        return total == null ? 0L : total;
    }

    /**
     * 매장 ID/이름 목록을 조회한다.
     *
     * @return {@link FindStoreDTO} 리스트
     */
    @Override
    public List<FindStoreDTO> findStoreName() {
        QStore store = QStore.store;
        return queryFactory
                .select(Projections.fields(FindStoreDTO.class,
                        store.id.as("storeId"),
                        store.name.as("storeName")
                ))
                .from(store)
                .orderBy(store.id.desc())
                .fetch();
    }

    /**
     * 매장 ID 기준으로 상세 정보를 조회한다.
     *
     * <p>
     * 매장 기본 정보 + 점주명 + 본사 담당자 정보까지 함께 조회한다.
     * </p>
     *
     * @param id 매장 ID
     * @return   {@link StoreDetailDTO}, 없으면 null
     */
    @Override
    public StoreDetailDTO findByStoreDetail(Long id) {
        QStore store = QStore.store;
        QStaffProfile staffProfile = QStaffProfile.staffProfile;
        QMember member = QMember.member;

        return queryFactory
                .select(Projections.fields(StoreDetailDTO.class,
                        store.id.as("storeId"),
                        store.name.as("storeName"),
                        store.status.as("storeStatus"),
                        store.businessRegistrationNumber.as("businessRegistrationNumber"),
                        store.phone.as("storePhone"),
                        store.location.as("storeLocation"),
                        store.type.as("storeType"),
                        store.contractStartDate.as("storeContractStartDate"),
                        store.contractAffiliateDate.as("storeContractAffiliateDate"),
                        store.contractTerm.as("storeContractTerm"),
                        store.affiliatePrice.as("storeAffiliatePrice"),
                        store.monthlySales.as("storeMonthlySales"),
                        store.royalty.as("royalty"),
                        store.comment.as("comment"),

                        member.name.as("memberName"),
                        member.email.as("memberEmail"),

                        ExpressionUtils.as(
                                JPAExpressions.select(staffProfile.staffName)
                                        .from(staffProfile)
                                        .where(
                                                staffProfile.store.id.eq(store.id),
                                                staffProfile.staffEmploymentType.eq(StaffEmploymentType.OWNER)
                                        )
                                        .orderBy(staffProfile.id.desc()) // 최근 1명
                                        .limit(1),
                                "staffName"
                        ),
                        store.totalEmployees.as("storeTotalEmployees")
                ))
                .from(store)
                .leftJoin(store.member, member)
                .where(store.id.eq(id))
                .fetchOne();
    }

    /**
     * 점주(OWNER) 직원 목록을 조회한다.
     *
     * @return 점주의 ID/이름 리스트
     */
    @Override
    public List<StaffNameDTO> ownerStaffOptions() {
        QStaffProfile staffProfile = QStaffProfile.staffProfile;

        return queryFactory
                .select(Projections.fields(StaffNameDTO.class,
                        staffProfile.id.as("staffId"),
                        staffProfile.staffName.as("staffName")
                ))
                .from(staffProfile)
                .where(staffProfile.staffEmploymentType.eq(StaffEmploymentType.OWNER))
                .orderBy(staffProfile.id.desc())
                .fetch();
    }

    /**
     * 본사 근무자(HQ WORKER + OFFICE 부서) 직원 목록을 조회한다.
     *
     * @return 본사 근무자 ID/이름 리스트
     */
    @Override
    public List<StaffNameDTO> hqWorkerStaffOptions() {
        QStaffProfile staffProfile = QStaffProfile.staffProfile;

        return queryFactory
                .select(Projections.fields(StaffNameDTO.class,
                        staffProfile.id.as("staffId"),
                        staffProfile.staffName.as("staffName")
                ))
                .from(staffProfile)
                .where(
                        staffProfile.staffEmploymentType.eq(StaffEmploymentType.WORKER),
                        staffProfile.staffDepartment.eq(StaffDepartment.OFFICE)
                )
                .orderBy(staffProfile.id.desc())
                .fetch();
    }

    // =========================
    // 요약 카드용 집계 4종
    // =========================

    /**
     * 전체 가맹점 수를 반환한다.
     *
     * @return 전체 매장 수
     */
    @Override
    public long countStoreAll() {
        QStore store = QStore.store;
        Long v = queryFactory
                .select(store.count())
                .from(store)
                .fetchOne();
        return v == null ? 0L : v;
    }

    /**
     * 운영 중(OPERATING) 가맹점 수를 반환한다.
     *
     * @return 운영 매장 수
     */
    @Override
    public long countActiveStore() {
        QStore store = QStore.store;
        Long v = queryFactory
                .select(store.count())
                .from(store)
                .where(store.status.eq(StoreStatus.OPERATING))
                .fetchOne();
        return v == null ? 0L : v;
    }

    /**
     * 전체 가맹점의 월 매출 평균을 반환한다.
     *
     * <p>NULL인 경우 0으로 처리하며, 소수점은 0자리로 반올림한다.</p>
     *
     * @return 평균 월 매출(반올림된 정수 형태)
     */
    @Override
    public BigDecimal avgMonthlySales() {
        QStore store = QStore.store;
        BigDecimal v = queryFactory
                .select(Expressions.numberTemplate(
                        BigDecimal.class,
                        "coalesce(avg({0}), 0)", store.monthlySales))
                .from(store)
                .fetchOne();
        if (v == null) return BigDecimal.ZERO;
        // 소수 없이 반올림 (원하면 scale 조절)
        return v.setScale(0, RoundingMode.HALF_UP);
    }

    /**
     * 전체 재직 직원 수를 반환한다.
     *
     * <p>퇴직일이 NULL인 인원만 집계한다.</p>
     *
     * @return 재직 중인 전체 직원 수
     */
    @Override
    public long totalEmployees() {
        QStaffProfile sp = QStaffProfile.staffProfile;

        Long v = queryFactory
                .select(sp.id.count())
                .from(sp)
                .where(sp.staffEndDate.isNull())   // 재직자만
                .fetchOne();

        return v == null ? 0L : v;
    }

    /**
     * 이메일로 회원을 조회하여 ID/이메일 정보를 반환한다.
     *
     * @param email 이메일 주소
     * @return      {@link FindMemberEmailtoIdDTO}, 없으면 null
     */
    @Override
    public FindMemberEmailtoIdDTO findMemberByEmail(String email) {
        QMember m = QMember.member;
        return queryFactory
                .select(Projections.fields(FindMemberEmailtoIdDTO.class,
                        m.email.as("email"),
                        m.id.as("id")
                ))
                .from(m)
                .where(m.email.eq(email))
                .fetchOne();
    }
}
