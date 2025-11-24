package com.boot.ict05_final_admin.domain.fcm.controller;

import com.boot.ict05_final_admin.domain.fcm.service.HqInventoryScanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * HQ 인벤토리 스캔 실행용 컨트롤러.
 *
 * <p>수동으로 스캔을 트리거하여 부족 재고/유통기한 임박 알림을 생성하고 전송하는 기능을 제공한다.
 * 스케줄러 외에 운영자가 수동 실행을 원할 때 사용한다.</p>
 *
 * @author 이경욱
 * @since 2025-11-10
 */
@RestController
@RequestMapping("/fcm/hq-scan")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('HQ','ADMIN')")
@Tag(name = "HQ 인벤토리 스캔", description = "재고/유효기간 스캔 및 알림 전송 수동 실행")
public class HqInventoryScanController {

    private final HqInventoryScanService service;

    /**
     * 모든 스캔(재고부족 + 유효기간임박) 동시 실행.
     *
     * @return 스캔 결과 요약(서비스 구현에 따른 자유 포맷)
     */
    @Operation(summary = "전체 스캔 실행", description = "재고부족 및 유효기간 임박 스캔을 모두 실행합니다.")
    @PostMapping("/run")
    public ResponseEntity<?> runAll() {
        return ResponseEntity.ok(service.scanAll());
    }

    /**
     * 재고 부족 항목 스캔 및 알림 전송 실행.
     *
     * @return 전송된 메시지 수
     */
    @Operation(summary = "재고 부족 스캔 실행", description = "재고 부족 항목을 스캔하고 알림을 전송합니다.")
    @PostMapping("/stock-low")
    public ResponseEntity<?> runStockLow() {
        int cnt = service.scanAndNotifyStockLow();
        return ResponseEntity.ok(java.util.Map.of("sent", cnt));
    }

    /**
     * 유통기한 임박 항목 스캔 및 알림 전송 실행.
     *
     * @return 전송된 메시지 수
     */
    @Operation(summary = "유통기한 임박 스캔 실행", description = "유통기한 임박 항목을 스캔하고 알림을 전송합니다.")
    @PostMapping("/expire-soon")
    public ResponseEntity<?> runExpireSoon() {
        int cnt = service.scanAndNotifyExpireSoon();
        return ResponseEntity.ok(java.util.Map.of("sent", cnt));
    }
}
