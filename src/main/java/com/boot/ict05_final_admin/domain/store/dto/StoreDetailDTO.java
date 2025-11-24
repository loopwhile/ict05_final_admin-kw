package com.boot.ict05_final_admin.domain.store.dto;

import com.boot.ict05_final_admin.domain.store.entity.StoreStatus;
import com.boot.ict05_final_admin.domain.store.entity.StoreType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 가맹점 상세 정보 조회 DTO.
 *
 * <p>가맹점 상세 화면 및 상세 API에서 사용되며,</p>
 * <p>매장의 모든 핵심 정보(연락처, 주소, 계약기간, 매출 등)를 포함한다.</p>
 *
 * <p>주요 포함 필드:</p>
 * <ul>
 *     <li>storeId : 매장 ID</li>
 *     <li>storeName : 매장명</li>
 *     <li>businessRegistrationNumber : 사업자등록번호</li>
 *     <li>storePhone : 연락처</li>
 *     <li>storeLocation : 사업장 주소</li>
 *     <li>storeStatus : 상태</li>
 *     <li>storeType : 가맹점/직영점</li>
 *     <li>storeTotalEmployees : 직원 수</li>
 *     <li>storeAffiliatePrice : 가맹비</li>
 *     <li>storeMonthlySales : 월 매출</li>
 *     <li>royalty : 월 로열티</li>
 * </ul>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StoreDetailDTO {

    /** 매장 고유 Id */
    @Schema(description = "매장 고유 ID", example = "1")
    private Long storeId;

    /** 가맹점명 */
    @Schema(description = "가맹점명", example = "코딩카페 강남점")
    private String storeName;

    /** 점주명 */
    @Schema(description = "점주명", example = "홍길동")
    private String staffName;

    /** 매장 사업자 등록번호 */
    @Schema(description = "사업자등록번호", example = "123-45-67890")
    private String businessRegistrationNumber;

    /** 매장 연락처 */
    @Schema(description = "매장 연락처", example = "02-1234-5678")
    private String storePhone;

    /** 매장 본사 담당자 */
    @Schema(description = "본사 담당자명", example = "김관리")
    private String memberName;

    /** 이메일 */
    @Schema(description = "본사 담당자 이메일", example = "manager@company.com")
    private String memberEmail;

    /** 사업장 주소 */
    @Schema(description = "사업장 주소", example = "서울시 강남구 테헤란로 123, 3층")
    private String storeLocation;

    /** 운영 상태 */
    @Schema(description = "매장 운영 상태", example = "ACTIVE")
    private StoreStatus storeStatus;

    /** 매장 구분 (직영점/가맹점)*/
    @Schema(description = "매장 구분", example = "FRANCHISE")
    private StoreType storeType;

    /** 매장 총 직원수 */
    @Schema(description = "총 직원 수", example = "8")
    private Integer storeTotalEmployees;

    /** 계약 시작일 */
    @Schema(description = "계약 시작일", example = "2023-01-01")
    private LocalDate storeContractStartDate;

    /** 계약 가맹일 */
    @Schema(description = "가맹 시작일", example = "2023-01-15")
    private LocalDate storeContractAffiliateDate;

    /** 매장계약기간 */
    @Schema(description = "계약 기간(개월)", example = "24")
    private int storeContractTerm;

    /** 가맹비 */
    @Schema(description = "가맹비", example = "10000000")
    private BigDecimal storeAffiliatePrice;

    /** 월매출 */
    @Schema(description = "월 매출", example = "4300000")
    private BigDecimal storeMonthlySales;

    /** 월 로열티 */
    @Schema(description = "월 로열티", example = "300000")
    private BigDecimal royalty;

    /** 특이사항 */
    @Schema(description = "특이사항", example = "매장 인테리어 예정")
    private String comment;

}
