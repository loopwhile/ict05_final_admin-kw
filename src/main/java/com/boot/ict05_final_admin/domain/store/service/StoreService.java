package com.boot.ict05_final_admin.domain.store.service;

import com.boot.ict05_final_admin.domain.auth.entity.Member;
import com.boot.ict05_final_admin.domain.member.repository.MemberRepository;
import com.boot.ict05_final_admin.domain.staffresources.entity.StaffProfile;
import com.boot.ict05_final_admin.domain.staffresources.repository.StaffRepository;
import com.boot.ict05_final_admin.domain.store.dto.*;
import com.boot.ict05_final_admin.domain.store.entity.Store;
import com.boot.ict05_final_admin.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 가맹점 관련 비즈니스 로직을 처리하는 서비스 클래스.
 *
 * <p>
 * 컨트롤러와 리포지토리 사이에서 트랜잭션 관리와 도메인 규칙을 담당하며,<br>
 * 가맹점의 등록, 수정, 조회, 통계(헤더 카드용) 기능을 제공한다.
 * </p>
 */
@RequiredArgsConstructor
@Slf4j
@Service
@Transactional
public class StoreService {

    /** 가맹점 데이터 접근(기본 CRUD + 커스텀 쿼리) */
    private final StoreRepository storeRepository;
    /** 직원(점주/본사 담당자) 조회용 리포지토리 */
    private final StaffRepository staffRepository;
    /** Member 엔티티 조회용 리포지토리 */
    private final MemberRepository memberRepository;

    @PersistenceContext
    private EntityManager em;

    /**
     * 검색 조건과 페이징 정보를 이용하여 가맹점 목록을 조회한다.
     *
     * <p>
     * 내부적으로 {@link StoreRepository#listStore(StoreSearchDTO, Pageable)} 를 호출하여
     * Querydsl 기반 동적 쿼리를 수행한다.
     * </p>
     *
     * @param storeSearchDTO 가맹점 검색 조건 DTO
     * @param pageable       페이지 정보 (페이지 번호, 크기, 정렬)
     * @return               페이징 처리된 가맹점 목록 DTO
     */
    public Page<StoreListDTO> selectAllOfficeStore(StoreSearchDTO storeSearchDTO, Pageable pageable) {
        return storeRepository.listStore(storeSearchDTO, pageable);
    }

    /**
     * 새로운 가맹점을 등록한다.
     *
     * <p>
     * - 주소(userAddress1, userAddress2)를 하나의 location으로 합친 뒤 저장<br>
     * - 선택된 본사 담당자(ID) → {@link StaffProfile} → 이메일 → {@link Member} 매핑<br>
     * - 매핑된 Member를 {@link Store} 엔티티의 member 필드에 연결하여 저장한다.
     * </p>
     *
     * @param dto 가맹점 등록 정보 DTO
     * @return    저장된 가맹점 ID
     * @throws IllegalArgumentException 본사 담당자 정보가 없거나 Member 매핑에 실패한 경우
     */
    public Long insertOfficeStore(StoreWriteFormDTO dto) {
        // ✅ 1. 주소 결합
        String address1 = dto.getUserAddress1();
        String address2 = dto.getUserAddress2();
        String address = (address1 == null ? "" : address1) + "," + (address2 == null ? "" : address2);
        dto.setStoreLocation(address);

        // ✅ 2. 본사 담당자 필수 검증
        if (dto.getHqWorkerStaffId() == null) {
            throw new IllegalArgumentException("본사 담당자는 반드시 선택해야 합니다.");
        }

        // ✅ 3. 본사 담당자 → Member 매핑
        StaffProfile hqWorker = staffRepository.findById(dto.getHqWorkerStaffId())
                .orElseThrow(() -> new IllegalArgumentException("선택한 본사 담당자 정보를 찾을 수 없습니다."));

        if (hqWorker.getStaffEmail() == null || hqWorker.getStaffEmail().isBlank()) {
            throw new IllegalArgumentException("본사 담당자의 이메일 정보가 존재하지 않습니다.");
        }

        FindMemberEmailtoIdDTO mDto = storeRepository.findMemberByEmail(hqWorker.getStaffEmail());
        if (mDto == null || mDto.getId() == null) {
            throw new IllegalArgumentException("본사 담당자 이메일에 해당하는 Member 계정을 찾을 수 없습니다.");
        }

        Member member = em.getReference(Member.class, mDto.getId());

        // ✅ 4. Store 엔티티 생성
        Store store = Store.builder()
                .name(dto.getStoreName())
                .member(member)
                .businessRegistrationNumber(dto.getBusinessRegistrationNumber())
                .phone(dto.getStorePhone())
                .status(dto.getStoreStatus())
                .type(dto.getStoreType())
                .totalEmployees(dto.getStoreTotalEmployees())
                .location(dto.getStoreLocation())
                .contractStartDate(dto.getStoreContractStartDate())
                .contractAffiliateDate(dto.getStoreContractAffiliateDate())
                .contractTerm(dto.getStoreContractTerm())
                .affiliatePrice(dto.getStoreAffiliatePrice())
                .monthlySales(dto.getStoreMonthlySales())
                .royalty(dto.getRoyalty())
                .comment(dto.getComment())
                .build();

        // ✅ 5. 저장
        Store saved = storeRepository.save(store);

        return saved.getId();
    }

    /**
     * 점주(OWNER)에 해당하는 직원 목록을 조회한다.
     *
     * @return 점주 ID/이름 리스트
     */
    @Transactional(readOnly = true)
    public List<StaffNameDTO> ownerOptions() {
        return storeRepository.ownerStaffOptions();
    }

    /**
     * 본사 근무자(HQ WORKER + OFFICE 부서) 직원 목록을 조회한다.
     *
     * @return 본사 근무자 ID/이름 리스트
     */
    @Transactional(readOnly = true)
    public List<StaffNameDTO> hqWorkerOptions() {
        return storeRepository.hqWorkerStaffOptions();
    }

    /**
     * 가맹점의 이름(및 간단 정보)을 조회한다.
     *
     * <p>주로 드롭다운, 선택 리스트 등에 사용된다.</p>
     *
     * @return 가맹점 표시용 DTO 리스트. 데이터가 없으면 빈 리스트 반환.
     * @see com.boot.ict05_final_admin.domain.store.repository.StoreRepository#findStoreName()
     */
    public List<FindStoreDTO> findStoreName() {
        return storeRepository.findStoreName();
    }

    /**
     * 가맹점 상세 정보를 조회한다.
     *
     * @param id 가맹점 ID
     * @return   상세 정보 DTO, 없으면 null
     */
    public StoreDetailDTO detailOfficeStore(Long id) {
        return storeRepository.findByStoreDetail(id);
    }

    /**
     * ID를 기준으로 가맹점 엔티티를 조회한다.
     *
     * @param id 가맹점 ID
     * @return   {@link Store} 엔티티, 존재하지 않으면 null
     */
    @Transactional(readOnly = true)
    public Store findById(Long id) {
        return storeRepository.findById(id).orElse(null);
    }

    /**
     * 기존 가맹점 정보를 수정한다.
     *
     * <p>
     * - 주소(userAddress1, userAddress2)를 다시 합쳐 storeLocation으로 세팅<br>
     * - ID로 기존 {@link Store} 를 조회한 후, {@link Store#updateStore(StoreModifyFormDTO)} 호출로 변경한다.
     * </p>
     *
     * @param dto 수정할 데이터 DTO
     * @return    수정된 가맹점 엔티티
     * @throws IllegalArgumentException 대상 가맹점이 존재하지 않을 경우
     */
    public Store storeModify(StoreModifyFormDTO dto) {

        String address = "";
        String address1 = dto.getUserAddress1();
        String address2 = dto.getUserAddress2();
        address = address1 + "," + address2;
        dto.setStoreLocation(address);

        Store store = findById(dto.getStoreId());
        if (store == null) throw new IllegalArgumentException("해당 가맹점이 존재하지 않습니다.");

        store.updateStore(dto);

        return store;
    }

    /**
     * 대시보드 헤더(요약 카드)에 보여줄 통계 데이터를 조회한다.
     *
     * <p>
     * - 전체 가맹점 수<br>
     * - 운영 중인 가맹점 수<br>
     * - 평균 월 매출<br>
     * - 전체 직원 수<br>
     * 를 키/값 형태의 Map으로 반환한다.
     * </p>
     *
     * @return 통계 값 맵 (totalStore, activeStore, avgMonthlySales, totalStaff)
     */
    @Transactional(readOnly = true)
    public Map<String, Object> listHeaderStats() {
        long total       = storeRepository.countStoreAll();
        long active      = storeRepository.countActiveStore();
        BigDecimal avg   = storeRepository.avgMonthlySales();
        long totalStaff  = storeRepository.totalEmployees();

        return Map.of(
                "totalStore",       total,
                "activeStore",      active,
                "avgMonthlySales",  avg,
                "totalStaff",       totalStaff
        );
    }

}
