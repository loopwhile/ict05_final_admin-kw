package com.boot.ict05_final_admin.domain.fcm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class HqNoticeFcmBridgeService {

    @Value("${toastlab.user-api.base-url}")
    private String userApiBase; // 예: http://localhost:8082/user

    private final RestClient restClient; // Spring 6+ 있으면 RestClient, 없으면 RestTemplate

    public void afterNoticeCreated(Long noticeId) {
        callUserNoticeEndpoint("/fcm/notice/created/" + noticeId);
    }

    public void afterNoticeUpdated(Long noticeId) {
        callUserNoticeEndpoint("/fcm/notice/updated/" + noticeId);
    }

    private void callUserNoticeEndpoint(String path) {
        try {
            var res = restClient
                    .post()
                    .uri(userApiBase + path)
                    .retrieve()
                    .toEntity(String.class);
            log.info("[HQ→USER FCM] {} status={}", path, res.getStatusCode());
        } catch (Exception e) {
            log.warn("[HQ→USER FCM] call failed path={}", path, e);
        }
    }
}
