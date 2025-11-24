package com.boot.ict05_final_admin.domain.store.dto;

import com.boot.ict05_final_admin.domain.store.entity.StoreStatus;
import com.boot.ict05_final_admin.domain.store.entity.StoreType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 가맹점 정보 수정 폼 DTO.
 *
 * <p>
 * 가맹점 정보 수정 시 클라이언트(화면/프론트)에서 전달하는 값들을 담는다.<br>
 * 수정 대상 매장의 식별자, 가맹점명, 사업자 등록번호, 연락처, 운영 상태, 매장 구분, 주소,
 * 계약 시작일, 가맹일, 계약 기간, 가맹비, 월 매출, 월 로열티, 특이사항 등을 포함한다.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "가맹점 정보 수정 폼 DTO")
public class StoreModifyFormDTO {

    /** 수정할 매장의 고유 Id */
    @Schema(description = "수정 대상 매장 ID", example = "1")
    private Long storeId;

    /** 가맹점명 */
    @Schema(description = "가맹점명", example = "코딩카페 강남점")
    @NotBlank(message = "가맹점명을 입력해주세요")
    @Size(max = 10, message = "가맹점명은 10자 이내로 입력해주세요")
    private String storeName;

    /** 매장 사업자 등록번호 */
    @Schema(description = "사업자등록번호", example = "123-45-67890")
    @NotBlank(message = "사업자등록번호를 입력해주세요")
    @Pattern(regexp = "^[0-9\\-]{9,13}$", message = "사업자등록번호는 숫자와 하이픈만 입력해주세요")
    private String businessRegistrationNumber;

    /** 매장 연락처 */
    @Schema(description = "매장 연락처", example = "02-1234-5678")
    @NotBlank(message = "매장 연락처를 입력해주세요")
    @Pattern(regexp = "^[0-9\\-]{9,13}$", message = "전화번호는 숫자와 하이픈만 입력해주세요")
    private String storePhone;

    /** 운영 상태 */
    @Schema(description = "운영 상태", example = "ACTIVE")
    @NotNull(message = "운영 상태를 선택해주세요")
    private StoreStatus storeStatus;

    /** 매장 구분 (직영점/가맹점)*/
    @Schema(description = "매장 구분", example = "FRANCHISE")
    @NotNull(message = "매장을 선택해주세요")
    private StoreType storeType;

    @Schema(description = "기본 주소", example = "서울시 강남구 테헤란로 123")
    @Size(max = 255, message = "주소는 255자 이내로 입력해주세요")
    private String userAddress1;

    @Schema(description = "상세 주소", example = "3층 301호")
    @Size(max = 255, message = "주소는 255자 이내로 입력해주세요")
    private String userAddress2;

    /** 사업장 주소 (full) */
    @Schema(description = "사업장 전체 주소 문자열", example = "서울시 강남구 테헤란로 123, 3층 301호")
    @Size(max = 255, message = "주소는 255자 이내로 입력해주세요")
    private String storeLocation;

    /** 총 직원 수 */
    @Schema(description = "총 직원 수", example = "5")
    @NotNull(message="총 직원수를 입력해주세요")
    @Min(value=1, message="총 직원수는 1 이상이어야 합니다")
    private Integer storeTotalEmployees;

    /** 계약일 */
    @Schema(description = "계약 시작일", example = "2023-01-01")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate storeContractStartDate;

    /** 가맹일 */
    @Schema(description = "가맹 시작일", example = "2023-01-15")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate storeContractAffiliateDate;

    /** 매장계약기간 */
    @Schema(description = "계약 기간(개월)", example = "24")
    @NotNull(message = "계약기간을 입력해주세요")
    @Min(value = 1, message = "계약기간은 1 이상이어야 합니다")
    private Integer storeContractTerm;

    /** 가맹비 */
    @Schema(description = "가맹비", example = "10000000")
    @NotNull(message = "가맹비를 입력해주세요")
    @Digits(integer = 14, fraction = 2, message = "가맹비는 소수점 둘째 자리까지 입력 가능합니다")
    @PositiveOrZero(message = "가맹비는 0 이상이어야 합니다")
    private BigDecimal storeAffiliatePrice;

    /** 월 매출 */
    @Schema(description = "월 매출", example = "5500000")
    @Digits(integer = 14, fraction = 2, message = "월 매출은 소수점 둘째 자리까지 입력 가능합니다")
    @PositiveOrZero(message = "월 매출은 0 이상이어야 합니다")
    private BigDecimal storeMonthlySales;

    /** 월 로열티 */
    @Schema(description = "월 로열티", example = "300000")
    @NotNull(message = "월 로열티를 입력해주세요")
    @Digits(integer = 14, fraction = 2, message = "월 로열티는 소수점 둘째 자리까지 입력 가능합니다")
    @PositiveOrZero(message = "월 로열티는 0 이상이어야 합니다")
    private BigDecimal royalty;

    /** 특이사항 */
    @Schema(description = "특이사항", example = "리모델링 예정, 야간 매출 높음")
    @Size(max = 1000, message = "특이사항은 1000자 이내로 입력해주세요")
    private String comment;

}
