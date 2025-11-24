package com.boot.ict05_final_admin.domain.inventory.dto;

import com.boot.ict05_final_admin.domain.inventory.entity.MaterialCategory;
import com.boot.ict05_final_admin.domain.inventory.entity.MaterialTemperature;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 재료 등록 폼 DTO.
 *
 * <p>재료 신규 등록 화면(Form)에서 서버로 전달되는 파라미터 컨테이너.</p>
 *
 * <p>검증/정책:</p>
 * <ul>
 *   <li>{@code name}, {@code baseUnit}, {@code salesUnit}: 공백 불가</li>
 *   <li>{@code conversionRate}, {@code materialTemperature}, {@code optimalQuantity}: 필수</li>
 *   <li>{@code conversionRate}: 판매단위 → 기본단위 변환비율(예: 1 BOX → 20 EA = 20)</li>
 *   <li>{@code optimalQuantity}: DECIMAL(15,3) 스케일 가정(서버/DB 정책에 맞춰 반올림)</li>
 *   <li>{@code code}, {@code supplier}: 선택(코드 자동생성 정책이 있으면 서비스 계층 처리)</li>
 * </ul>
 */
@Data
public class MaterialWriteFormDTO {

    /** 작성자(회원) FK (선택) */
    private Long memberIdFk;

    /** 재료 코드(선택, 정책에 따라 자동 생성 가능) */
    private String code;

    /** 재료 카테고리(선택: 화면에서 선지정 가능. 필수화는 정책에 따름) */
    private MaterialCategory materialCategory;

    /** 재료명(필수) */
    @NotBlank(message = "재료명을 입력해주세요")
    private String name;

    /** 기본 단위(소진 단위, 필수) */
    @NotBlank(message = "기본 단위를 입력해주세요")
    private String baseUnit;

    /** 판매 단위(필수) */
    @NotBlank(message = "판매 단위를 입력해주세요")
    private String salesUnit;

    /** 판매단위 → 기본단위 변환비율(필수, 예: 1 BOX → 20 EA = 20) */
    @NotNull(message = "변환비율을 입력해주세요")
    private Integer conversionRate;

    /** 공급업체명(선택) */
    private String supplier;

    /** 보관 온도(필수) */
    @NotNull(message = "보관 온도를 선택해주세요")
    private MaterialTemperature materialTemperature;

    /** 본사 기준 적정 재고 수량(필수, DECIMAL(15,3) 가정) */
    @NotNull(message = "적정 수량을 입력해주세요")
    private BigDecimal optimalQuantity;
}
