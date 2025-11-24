package com.boot.ict05_final_admin.domain.fcm.service;

import com.boot.ict05_final_admin.domain.fcm.dto.NoticeFcmEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class NoticeFcmEventListener {

    private final HqNoticeFcmBridgeService hqNoticeFcmBridgeService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onNoticeEvent(NoticeFcmEvent event) {
        Long noticeId = event.getNoticeId();

        try {
            switch (event.getType()) {
                case CREATED -> {
                    log.info("[NoticeFcmEventListener] AFTER_COMMIT created noticeId={}", noticeId);
                    hqNoticeFcmBridgeService.afterNoticeCreated(noticeId);
                }
                case UPDATED -> {
                    log.info("[NoticeFcmEventListener] AFTER_COMMIT updated noticeId={}", noticeId);
                    hqNoticeFcmBridgeService.afterNoticeUpdated(noticeId);
                }
            }
        } catch (Exception e) {
            log.warn("[NoticeFcmEventListener] bridge call failed noticeId={} type={}",
                    noticeId, event.getType(), e);
        }
    }
}
