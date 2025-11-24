package com.boot.ict05_final_admin.domain.fcm.repository;

import com.boot.ict05_final_admin.domain.fcm.dto.HqExpireSoonCandidate;
import com.boot.ict05_final_admin.domain.fcm.dto.HqStockLowCandidate;

import java.time.LocalDate;
import java.util.List;

/**
 * HQ 인벤토리 스캐너용 커스텀 리포지토리 인터페이스.
 *
 * <p>본사에서 재고부족 또는 유통기한 임박 후보를 조회하기 위한 메서드를 정의한다.
 * 실제 조회 구현은 QueryDSL 기반 구현체에서 제공되며, 운영환경 설정에 따라 활성화된다.</p>
 *
 * @author 이경욱
 * @since 2025-11-10
 */
public interface HqInventoryScannerRepository {

    /**
     * 본사 재고 부족 후보 목록을 조회한다.
     *
     * @param maxRows 조회할 최대 행 수(안전장치)
     * @return 재고 부족 후보 DTO 리스트 (없으면 빈 리스트)
     */
    List<HqStockLowCandidate> findStockLow(int maxRows);

    /**
     * 본사 유통기한 임박 후보 목록을 조회한다.
     *
     * @param today         기준 날짜(보통 LocalDate.now())
     * @param daysThreshold 임박 기준 일수 (예: 3일 이내)
     * @param maxRows       조회할 최대 행 수(안전장치)
     * @return 유통기한 임박 후보 DTO 리스트 (없으면 빈 리스트)
     */
    List<HqExpireSoonCandidate> findExpireSoon(LocalDate today, int daysThreshold, int maxRows);
}
