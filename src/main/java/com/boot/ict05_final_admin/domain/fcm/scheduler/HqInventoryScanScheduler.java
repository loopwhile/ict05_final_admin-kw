package com.boot.ict05_final_admin.domain.fcm.scheduler;

import com.boot.ict05_final_admin.domain.fcm.config.FcmScannerProperties;
import com.boot.ict05_final_admin.domain.fcm.service.HqInventoryScanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 본사 인벤토리 스캐너 스케줄러.
 *
 * <p>설정 값(fcm.scanner.cron)에 따라 주기적으로 재고 부족/유통기한 임박 스캔을 실행하고
 * 결과에 따라 알림을 전송한다. 스케줄러 활성화는 {@code fcm.scanner.enabled=true} 조건으로 제어한다.</p>
 *
 * <p>기본 CRON: {@code 0 0/30 * * * *} (30분 간격)</p>
 *
 * @author 이경욱
 * @since 2025-11-10
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "fcm.scanner.enabled", havingValue = "true", matchIfMissing = false)
@Slf4j
public class HqInventoryScanScheduler {

    private final HqInventoryScanService service;
    private final FcmScannerProperties props;

    /**
     * 설정된 CRON 표현식에 따라 인벤토리 스캔을 실행한다.
     * 성공/실패 결과는 운영 로그로 남긴다.
     *
     * 실행 주기:
     * - 기본: 30분마다 (0 0/30 * * * *)
     * - 테스트:
     *   1분마다   -> fcm.scanner.cron=0 * * * * *
     *   30초마다 -> fcm.scanner.cron=&#42;&#47;30 * * * * *
     *   변경하고 싶으면 이부분(프로퍼티 fcm.scanner.cron) 변경
     */
    @Scheduled(cron = "${fcm.scanner.cron:0 0/30 * * * *}")
    public void run() {
        try {
            var res = service.scanAll();
            log.info("[HQ-Scanner][CRON] done: {}", res);
        } catch (Exception e) {
            log.warn("[HQ-Scanner][CRON] failed: {}", e.getMessage());
        }
    }
}
