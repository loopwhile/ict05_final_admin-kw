package com.boot.ict05_final_admin.domain.store.entity;

import com.boot.ict05_final_admin.domain.auth.entity.Member;
import com.boot.ict05_final_admin.domain.store.dto.StoreModifyFormDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 가맹점(Store) JPA 엔티티.
 *
 * <p>
 * 데이터베이스 테이블 <code>store</code> 과 매핑되며,<br>
 * 매장의 기본 정보, 계약 정보, 매출 정보, 운영 상태 등을 포함한다.
 * </p>
 */
@Entity
@Table(name = "store")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "가맹점 엔티티")
public class Store {

    /** 가맹점 시퀀스 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_id", columnDefinition = "BIGINT UNSIGNED")
    @Schema(description = "가맹점 ID", example = "1")
    private Long id;

    /** 본사 담당자 시퀀스 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id_fk", nullable = true)
    @Schema(description = "본사 담당자 Member 엔티티(FK)", nullable = true)
    private Member member;

    /** 가맹점명 */
    @Column(name = "store_name", length = 150, nullable = false)
    @Schema(description = "가맹점명", example = "코딩카페 강남점")
    private String name;

    /** 가맹점 주소 */
    @Column(name = "store_location", length = 255)
    @Schema(description = "전체 주소", example = "서울시 강남구 테헤란로 123, 3층")
    private String location;

    /** 가맹점 구분 */
    @Enumerated(EnumType.STRING)
    @Column(name = "store_type", length = 10)
    @Builder.Default
    @Schema(description = "매장 구분", example = "FRANCHISE")
    private StoreType type = StoreType.FRANCHISE;

    /** 가맹점 상태 */
    @Enumerated(EnumType.STRING)
    @Column(name = "store_status", length = 10)
    @Builder.Default
    @Schema(description = "매장 운영 상태", example = "OPERATING")
    private StoreStatus status = StoreStatus.OPERATING;

    /** 총 직원수 */
    @Column(name = "store_total_employees")
    @Schema(description = "총 직원 수", example = "7")
    private Integer totalEmployees;

    /** 가맹점 계약 시작일 */
    @Column(name = "store_contract_start_date")
    @Schema(description = "계약 시작일", example = "2023-01-01")
    private LocalDate contractStartDate;

    /** 가맹점 계약 가맹일 */
    @Column(name = "store_contract_affiliate_date")
    @Schema(description = "가맹일", example = "2023-01-15")
    private LocalDate contractAffiliateDate;

    /** 가맹점 계약 기간 */
    @Column(name = "store_contract_term")
    @Schema(description = "계약 기간(개월)", example = "24")
    private Integer contractTerm;

    /** 가맹점 가맹비 */
    @Column(name = "store_affiliate_price", precision = 14, scale = 2)
    @Schema(description = "가맹비", example = "10000000")
    private BigDecimal affiliatePrice;

    /** 월매출 */
    @Column(name = "store_monthly_sales", precision = 14, scale = 2)
    @Schema(description = "월 매출", example = "4200000")
    private BigDecimal monthlySales;

    /** 가맹점 연락처 */
    @Column(name = "store_phone", length = 50)
    @Schema(description = "매장 연락처", example = "02-1234-5678")
    private String phone;

    /** 사업자등록번호 */
    @Column(name = "business_registration_number", length = 50)
    @Schema(description = "사업자등록번호", example = "123-45-67890")
    private String businessRegistrationNumber;

    /** 특이사항 */
    @Column(name = "store_comment", columnDefinition = "TEXT")
    @Schema(description = "특이사항", example = "리모델링 예정, 주말 매출 높음")
    private String comment;

    /** 가맹점 로열티 */
    @Column(name = "store_royalty", precision = 8, scale = 4)
    @Schema(description = "월 로열티", example = "350000")
    private BigDecimal royalty;

    /**
     * 가맹점 정보를 수정하는 메서드.
     *
     * <p>
     * 전달받은 {@link StoreModifyFormDTO} 의 데이터를 기준으로 가맹점 엔티티의 필드 값을 업데이트한다.
     * </p>
     *
     * @param dto 수정할 정보가 담긴 DTO
     */
    public void updateStore(StoreModifyFormDTO dto) {
        this.name = dto.getStoreName();
        this.location = dto.getStoreLocation();
        this.type = dto.getStoreType();
        this.status = dto.getStoreStatus();
        this.totalEmployees = dto.getStoreTotalEmployees();
        this.contractStartDate = dto.getStoreContractStartDate();
        this.contractAffiliateDate = dto.getStoreContractAffiliateDate();
        this.contractTerm = dto.getStoreContractTerm();
        this.affiliatePrice = dto.getStoreAffiliatePrice();
        this.monthlySales = dto.getStoreMonthlySales();
        this.phone = dto.getStorePhone();
        this.businessRegistrationNumber = dto.getBusinessRegistrationNumber();
        this.comment = dto.getComment();
        this.royalty = dto.getRoyalty();
    }
}
