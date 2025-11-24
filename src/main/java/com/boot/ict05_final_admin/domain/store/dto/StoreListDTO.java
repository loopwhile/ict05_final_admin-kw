package com.boot.ict05_final_admin.domain.store.dto;

import com.boot.ict05_final_admin.domain.store.entity.StoreStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 가맹점(매장) 목록 조회용 DTO.
 *
 * <p>가맹점 목록 조회 시 사용되는 데이터 전송 객체(DTO)로,</p>
 * <p>매장 리스트 화면 및 목록 API에서 사용된다.</p>
 *
 * <p>주요 포함 필드:</p>
 * <ul>
 *     <li>storeId : 고유 ID</li>
 *     <li>storeName : 매장명</li>
 *     <li>storeStatus : 운영 상태</li>
 *     <li>staffName : 점주명</li>
 *     <li>storePhone : 연락처</li>
 *     <li>storeMonthlySales : 월 매출</li>
 *     <li>storeTotalEmployees : 직원 수</li>
 * </ul>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoreListDTO  {

    /** 매장 고유 Id */
    @Schema(description = "매장 고유 ID", example = "1")
    private Long storeId;

    /** 가맹점명 */
    @Schema(description = "가맹점명", example = "코딩카페 강남점")
    private String storeName;

    /** 운영 상태 */
    @Schema(description = "매장 운영 상태", example = "ACTIVE")
    private StoreStatus storeStatus;

    /** 점주명 */
    @Schema(description = "점주명(사장님 이름)", example = "홍길동")
    private String staffName;

    /** 매장 연락처 */
    @Schema(description = "매장 연락처", example = "02-1234-5678")
    private String storePhone;

    /** 매장 월매출 */
    @Schema(description = "월 매출", example = "5400000")
    private BigDecimal storeMonthlySales;

    /** 매장 총 직원수 */
    @Schema(description = "매장 총 직원 수", example = "7")
    private Integer storeTotalEmployees;

}
