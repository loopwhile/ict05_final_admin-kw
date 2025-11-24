package com.boot.ict05_final_admin.domain.fcm.service;

import com.boot.ict05_final_admin.domain.fcm.config.FcmScannerProperties;
import com.boot.ict05_final_admin.domain.fcm.dto.HqExpireSoonCandidate;
import com.boot.ict05_final_admin.domain.fcm.dto.HqStockLowCandidate;
import com.boot.ict05_final_admin.domain.fcm.dto.HqTopic;
import com.boot.ict05_final_admin.domain.fcm.entity.AppType;
import com.boot.ict05_final_admin.domain.fcm.repository.HqInventoryScannerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 본사 인벤토리 스캔 및 알림 발송 서비스.
 *
 * <p>QueryDSL 스캐너를 통해 재고 부족/유통기한 임박 후보를 조회하고,
 * 템플릿을 렌더링하여 HQ 토픽으로 알림을 전송한다.</p>
 *
 * <ul>
 *   <li>{@link #scanAndNotifyStockLow()} 재고 부족 스캔 및 전송</li>
 *   <li>{@link #scanAndNotifyExpireSoon()} 유통기한 임박 스캔 및 전송</li>
 *   <li>{@link #scanAll()} 두 스캔 모두 실행</li>
 * </ul>
 *
 * @author 이경욱
 * @since 2025-11-10
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HqInventoryScanService {

    private final HqInventoryScannerRepository repo;
    private final FcmService fcmService;
    private final FcmScannerProperties props;

    /**
     * 재고 부족 후보를 스캔하고 HQ 토픽으로 알림을 전송한다.
     *
     * @return 전송 성공 건수
     */
    public int scanAndNotifyStockLow() {
        List<HqStockLowCandidate> rows = repo.findStockLow(props.getStockLowMax());
        int sent = 0;
        for (HqStockLowCandidate r : rows) {
            Map<String, Object> vars = Map.of(
                    "materialName", r.getMaterialName(),
                    "qty",          r.getQty(),
                    "threshold",    r.getThreshold()
            );
            String title = fcmService.renderTitle("HQ_STOCK_LOW", vars);
            String body  = fcmService.renderBody ("HQ_STOCK_LOW", vars);

            Map<String, String> data = new HashMap<>();
            data.put("type", "HQ_STOCK_LOW");
            data.put("materialName", r.getMaterialName());
            data.put("link", "/admin/inventory/list");

            try {
                fcmService.sendToTopic(AppType.HQ, HqTopic.STOCK_LOW, title, body, data);
                sent++;
            } catch (Exception e) {
                log.warn("[HQ-Scanner] STOCK_LOW send failed: {}", e.getMessage());
            }
        }
        log.info("[HQ-Scanner] STOCK_LOW candidates={}, sent={}", rows.size(), sent);
        return sent;
    }

    /**
     * 유통기한 임박 후보를 스캔하고 HQ 토픽으로 알림을 전송한다.
     *
     * @return 전송 성공 건수
     */
    public int scanAndNotifyExpireSoon() {
        int days = Math.max(1, props.getExpireSoonDaysDefault());
        LocalDate today = LocalDate.now();

        List<HqExpireSoonCandidate> rows = repo.findExpireSoon(today, days, props.getExpireSoonMax());
        int sent = 0;
        for (HqExpireSoonCandidate r : rows) {
            Map<String, Object> vars = Map.of(
                    "materialName", r.getMaterialName(),
                    "days",         r.getDaysLeft() != null ? r.getDaysLeft() : days,
                    "lot",          r.getLot() != null ? r.getLot() : "-"
            );
            String title = fcmService.renderTitle("HQ_EXPIRE_SOON", vars);
            String body  = fcmService.renderBody ("HQ_EXPIRE_SOON", vars);

            Map<String, String> data = new HashMap<>();
            data.put("type", "HQ_EXPIRE_SOON");
            data.put("materialName", r.getMaterialName());
            data.put("days", String.valueOf(r.getDaysLeft() != null ? r.getDaysLeft() : days));
            if (r.getLot() != null) data.put("lot", r.getLot());
            data.put("link", "/admin/inventory/list");

            try {
                fcmService.sendToTopic(AppType.HQ, HqTopic.EXPIRE_SOON, title, body, data);
                sent++;
            } catch (Exception e) {
                log.warn("[HQ-Scanner] EXPIRE_SOON send failed: {}", e.getMessage());
            }
        }
        log.info("[HQ-Scanner] EXPIRE_SOON candidates={}, sent={}", rows.size(), sent);
        return sent;
    }

    /**
     * 재고 부족/유통기한 임박 스캔을 모두 실행한다.
     *
     * @return {"stockLow": 전송수, "expireSoon": 전송수}
     */
    public Map<String, Integer> scanAll() {
        int a = scanAndNotifyStockLow();
        int b = scanAndNotifyExpireSoon();
        return Map.of("stockLow", a, "expireSoon", b);
    }
}
