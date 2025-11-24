package com.boot.ict05_final_admin.domain.fcm.repository;

import com.boot.ict05_final_admin.domain.fcm.dto.HqExpireSoonCandidate;
import com.boot.ict05_final_admin.domain.fcm.dto.HqStockLowCandidate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * HQ 인벤토리 스캐너의 기본 No-Op 구현체.
 *
 * <p>QueryDSL 기반의 실제 구현이 활성화되지 않은 경우(프로퍼티 {@code fcm.scanner.querydsl=false} 또는 미설정)
 * 이 클래스가 빈으로 등록되어 안전하게 빈 결과를 반환한다. 디버그 로그를 남긴다.</p>
 *
 * @author 이경욱
 * @since 2025-11-10
 */
@Repository
@ConditionalOnProperty(name = "fcm.scanner.querydsl", havingValue = "false", matchIfMissing = true)
@Slf4j
public class HqInventoryScannerRepositoryNoop implements HqInventoryScannerRepository {

    /**
     * 재고 부족 후보를 조회하는 No-Op 구현.
     *
     * @param maxRows 조회할 최대 행 수
     * @return 항상 빈 리스트를 반환
     */
    @Override
    public List<HqStockLowCandidate> findStockLow(int maxRows) {
        log.debug("[HQ-Scanner][NoOp] findStockLow(maxRows={})", maxRows);
        return List.of();
    }

    /**
     * 유통기한 임박 후보를 조회하는 No-Op 구현.
     *
     * @param today         기준 날짜
     * @param daysThreshold 임박 기준 일수
     * @param maxRows       조회할 최대 행 수
     * @return 항상 빈 리스트를 반환
     */
    @Override
    public List<HqExpireSoonCandidate> findExpireSoon(LocalDate today, int daysThreshold, int maxRows) {
        log.debug("[HQ-Scanner][NoOp] findExpireSoon(today={}, days={}, max={})",
                today, daysThreshold, maxRows);
        return List.of();
    }
}
