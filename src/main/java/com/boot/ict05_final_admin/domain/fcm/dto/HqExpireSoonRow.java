package com.boot.ict05_final_admin.domain.fcm.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 유통기한 임박 항목의 읽기 전용 행 DTO.
 *
 * @param materialId   재료 ID
 * @param materialName 재료명
 * @param batchId      배치(로트) ID
 * @param expireDate   유통기한 일자
 * @param daysLeft     남은 일수
 * @param quantity     해당 배치의 수량
 *
 * @author 이경욱
 * @since 2025-11-10
 */
public record HqExpireSoonRow(
        Long materialId,
        String materialName,
        Long batchId,
        LocalDate expireDate,
        Integer daysLeft,
        BigDecimal quantity
) { }
