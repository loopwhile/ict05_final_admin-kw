package com.boot.ict05_final_admin.domain.fcm.scheduler;

import com.boot.ict05_final_admin.domain.fcm.entity.AppType;
import com.boot.ict05_final_admin.domain.fcm.service.FcmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 본사 아침 리마인드 알림 스케줄러.
 *
 * <p>매일 아침 본사(HQ) 공용 토픽으로 재고/공지 확인을 유도하는 리마인드 알림을 보낸다.
 * 실제 스케줄 주기는 운영 환경에서 @Scheduled로 제어한다.</p>
 *
 * @author 이경욱
 * @since 2025-11-10
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class HqDailyReminderScheduler {

    private final FcmService fcmService;

    /**
     * 아침 리마인드 알림을 HQ 공용 토픽(hq-all)으로 전송한다.
     * 예외 발생 시 경고 로그만 남긴다.
     */
    public void sendMorningReminder() {
        try {
            fcmService.sendToTopic(
                    AppType.HQ, "hq-all",
                    "[본사] 아침 리마인드", "금일 재고/공지 확인을 진행해주세요.",
                    Map.of("type", "HQ_REMINDER", "link", "/admin/dashboard"));
        } catch (Exception e) {
            log.warn("Failed to send morning reminder: {}", e.getMessage());
        }
    }
}
