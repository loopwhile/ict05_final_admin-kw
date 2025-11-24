package com.boot.ict05_final_admin.domain.fcm.service;

import com.boot.ict05_final_admin.domain.fcm.config.FcmTopicPolicyProperties;
import com.boot.ict05_final_admin.domain.fcm.config.FcmWebpushProperties;
import com.boot.ict05_final_admin.domain.fcm.dto.FcmRegisterTokenRequest;
import com.boot.ict05_final_admin.domain.fcm.dto.HqTopic;
import com.boot.ict05_final_admin.domain.fcm.entity.AppType;
import com.boot.ict05_final_admin.domain.fcm.entity.FcmDeviceToken;
import com.boot.ict05_final_admin.domain.fcm.entity.FcmSendLog;
import com.boot.ict05_final_admin.domain.fcm.entity.FcmTemplate;
import com.boot.ict05_final_admin.domain.fcm.repository.FcmDeviceTokenRepository;
import com.boot.ict05_final_admin.domain.fcm.repository.FcmSendLogRepository;
import com.boot.ict05_final_admin.domain.fcm.repository.FcmTemplateRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.*;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * FCM 토큰 등록/전송/구독 및 전송 로그 관리를 담당하는 서비스.
 *
 * <p>FirebaseApp 비활성화 환경에서는 전송 메서드가 예외를 던지니,
 * 운영 설정에서 {@code fcm.enabled=true}와 서비스 계정 경로 설정을 확인해야 한다.</p>
 *
 * <ul>
 *   <li>토큰 등록/해제</li>
 *   <li>템플릿 렌더링</li>
 *   <li>토큰/토픽 전송 (WebPush 설정 적용)</li>
 *   <li>토픽 구독/해제</li>
 *   <li>전송 로그 저장</li>
 * </ul>
 *
 * @author 이경욱
 * @since 2025-11-10
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FcmService {

    private final FcmDeviceTokenRepository tokenRepository;
    private final FcmTemplateRepository templateRepository;
    private final FcmSendLogRepository logRepository;
    private final ObjectMapper objectMapper;
    private final FcmTopicPolicyProperties topicProps;
    private final FcmWebpushProperties webpushProps;

    @Autowired(required = false)
    @Nullable
    private FirebaseApp firebaseApp;

    private static final java.util.regex.Pattern TOPIC_PATTERN =
            java.util.regex.Pattern.compile("^[a-z0-9-]{1,64}$");

    /**
     * HQ 세션 사용자의 토큰을 등록/업서트한다.
     *
     * @param req             등록 요청
     * @param sessionMemberId 세션에서 추출된 HQ 사용자 ID(우선 적용)
     */
    @Transactional
    public void registerToken(FcmRegisterTokenRequest req, Long sessionMemberId) {
        String token = req.token();
        FcmDeviceToken row = tokenRepository.findByToken(token)
                .orElse(FcmDeviceToken.builder().token(token).build());

        row.setAppType(req.appType());
        row.setPlatform(req.platform());
        row.setDeviceId(req.deviceId());
        row.setMemberIdFk(sessionMemberId != null ? sessionMemberId : req.memberIdFk());
        row.setIsActive(true);
        row.setLastSeenAt(LocalDateTime.now());

        tokenRepository.save(row);
    }

    /**
     * 토큰을 비활성화 처리한다(논리적 해제).
     *
     * @param token           대상 토큰
     * @param sessionMemberId 세션 사용자 ID(필요 시 소유자 검증에 활용)
     */
    @Transactional
    public void unregisterToken(String token, Long sessionMemberId) {
        tokenRepository.findByToken(token).ifPresent(row -> {
            row.setIsActive(false);
            row.setLastSeenAt(LocalDateTime.now());
        });
    }

    /**
     * 템플릿 코드와 변수로 제목을 렌더링한다.
     *
     * @param templateCode 템플릿 코드
     * @param vars         템플릿 변수 맵
     * @return 렌더링된 제목
     */
    @Transactional(readOnly = true)
    public String renderTitle(String templateCode, Map<String, Object> vars) {
        return render(templateCode, true, vars);
    }

    /**
     * 템플릿 코드와 변수로 본문을 렌더링한다.
     *
     * @param templateCode 템플릿 코드
     * @param vars         템플릿 변수 맵
     * @return 렌더링된 본문
     */
    @Transactional(readOnly = true)
    public String renderBody(String templateCode, Map<String, Object> vars) {
        return render(templateCode, false, vars);
    }

    private String render(String code, boolean title, Map<String, Object> vars) {
        FcmTemplate t = templateRepository.findByTemplateCode(code)
                .orElseThrow(() -> new IllegalArgumentException("No template: " + code));
        String src = title ? t.getTitleTemplate() : t.getBodyTemplate();
        if (vars == null || vars.isEmpty()) return src;
        String out = src;
        for (Map.Entry<String, Object> e : vars.entrySet()) {
            out = out.replace("{" + e.getKey() + "}", String.valueOf(e.getValue()));
        }
        return out;
    }

    /**
     * 단일 토큰으로 알림을 전송하고 결과를 로그로 저장한다.
     *
     * @param appType 앱 타입
     * @param token   대상 토큰
     * @param title   제목
     * @param body    본문
     * @param data    데이터 맵(1KB 초과 시 핵심 필드만 유지)
     * @return 메시지 ID
     */
    public String sendToToken(AppType appType, String token, String title, String body, Map<String, String> data) {
        ensureFirebase();
        try {
            Map<String, String> safeData = sanitizeData(data);
            Message msg = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                    .setWebpushConfig(webpushConfigFor(safeData))
                    .putAllData(safeData)
                    .build();

            String msgId = FirebaseMessaging.getInstance(firebaseApp).send(msg);
            persistLog(appType, null, token, title, body, safeData, msgId, null, null, null, null);
            return msgId;
        } catch (Exception ex) {
            log.error("[FCM] sendToToken failed: {}", ex.getMessage(), ex);
            persistLog(appType, null, token, title, body, data, null, ex.getMessage(), null, null, null);
            throw new IllegalStateException(ex);
        }
    }

    /**
     * 토픽으로 알림을 전송하고 결과를 로그로 저장한다.
     *
     * @param appType 앱 타입
     * @param topic   대상 토픽
     * @param title   제목
     * @param body    본문
     * @param data    데이터 맵(1KB 초과 시 핵심 필드만 유지)
     * @return 메시지 ID
     */
    public String sendToTopic(AppType appType, String topic, String title, String body, Map<String, String> data) {
        ensureFirebase();
        try {
            topic = validateTopic(topic);
            Map<String, String> safeData = sanitizeData(data);
            Message msg = Message.builder()
                    .setTopic(topic)
                    .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                    .setWebpushConfig(webpushConfigFor(safeData))
                    .putAllData(safeData)
                    .build();

            String msgId = FirebaseMessaging.getInstance(firebaseApp).send(msg);
            persistLog(appType, topic, null, title, body, safeData, msgId, null, null, null, null);
            return msgId;
        } catch (Exception ex) {
            log.error("[FCM] sendToTopic failed: {}", ex.getMessage(), ex);
            persistLog(appType, topic, null, title, body, data, null, ex.getMessage(), null, null, null);
            throw new IllegalStateException(ex);
        }
    }

    private void ensureFirebase() {
        if (firebaseApp == null) {
            throw new IllegalStateException("FCM is disabled (no FirebaseApp bean). Set fcm.enabled=true");
        }
    }

    private String validateTopic(String topic) {
        if (topic == null) throw new IllegalArgumentException("Invalid topic name: null");
        topic = topic.trim().toLowerCase();
        if (!TOPIC_PATTERN.matcher(topic).matches()) {
            throw new IllegalArgumentException("Invalid topic pattern: " + topic);
        }
        if (topicProps.isRestrict() && !HqTopic.isAllowed(topic)) {
            throw new IllegalArgumentException("Topic not allowed in HQ: " + topic);
        }
        return topic;
    }

    private Map<String, String> sanitizeData(Map<String, String> data) {
        if (data == null || data.isEmpty()) return Map.of();
        try {
            byte[] raw = objectMapper.writeValueAsBytes(data);
            if (raw.length <= 1024) return data;
        } catch (Exception ignore) {
            return data;
        }
        Map<String, String> slim = new java.util.HashMap<>();
        if (data.containsKey("type")) slim.put("type", data.get("type"));
        if (data.containsKey("link")) slim.put("link", data.get("link"));
        return slim;
    }

    private void persistLog(AppType appType, String topic, String token,
                            String title, String body, Map<String, String> data,
                            String msgId, String error,
                            Long storeId, Long memberId, Long staffId) {
        try {
            FcmSendLog logRow = FcmSendLog.builder()
                    .appType(appType)
                    .topic(topic)
                    .token(token)
                    .title(title)
                    .body(body)
                    .dataJson(data == null ? null : objectToJsonSafe(data))
                    .resultMessageId(msgId)
                    .resultError(error)
                    .storeIdFk(storeId)
                    .memberIdFk(memberId)
                    .staffIdFk(staffId)
                    .build();
            logRepository.save(logRow);
        } catch (Exception e) {
            log.warn("[FCM] persistLog failed: {}", e.getMessage());
        }
    }

    private String objectToJsonSafe(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 회원의 활성 토큰 전체를 지정 토픽에 구독시킨다.
     *
     * @param topic    토픽
     * @param memberId 회원 ID
     */
    public void subscribeToTopic(String topic, Long memberId) {
        ensureFirebase();
        topic = validateTopic(topic);
        var tokens = tokenRepository.findActiveTokensForHqMember(memberId);
        if (tokens == null || tokens.isEmpty()) {
            log.info("[FCM] subscribe skipped (no active tokens) memberId={}", memberId);
            return;
        }
        for (int i = 0; i < tokens.size(); i += 1000) {
            var batch = tokens.subList(i, Math.min(i + 1000, tokens.size()));
            try {
                TopicManagementResponse resp =
                        FirebaseMessaging.getInstance(firebaseApp).subscribeToTopic(batch, topic);
                log.info("[FCM] subscribeToTopic topic='{}' success={}, failure={}",
                        topic, resp.getSuccessCount(), resp.getFailureCount());
            } catch (Exception e) {
                log.warn("[FCM] subscribeToTopic failed (batch size={}): {}", batch.size(), e.getMessage());
            }
        }
    }

    /**
     * 회원의 활성 토큰 전체를 지정 토픽에서 구독 해제한다.
     *
     * @param topic    토픽
     * @param memberId 회원 ID
     */
    public void unsubscribeFromTopic(String topic, Long memberId) {
        ensureFirebase();
        topic = validateTopic(topic);
        var tokens = tokenRepository.findActiveTokensForHqMember(memberId);
        if (tokens == null || tokens.isEmpty()) {
            log.info("[FCM] unsubscribe skipped (no active tokens) memberId={}", memberId);
            return;
        }
        for (int i = 0; i < tokens.size(); i += 1000) {
            var batch = tokens.subList(i, Math.min(i + 1000, tokens.size()));
            try {
                TopicManagementResponse resp =
                        FirebaseMessaging.getInstance(firebaseApp).unsubscribeFromTopic(batch, topic);
                log.info("[FCM] unsubscribeFromTopic topic='{}' success={}, failure={}",
                        topic, resp.getSuccessCount(), resp.getFailureCount());
            } catch (Exception e) {
                log.warn("[FCM] unsubscribeFromTopic failed (batch size={}): {}", batch.size(), e.getMessage());
            }
        }
    }

    /**
     * WebPush 설정(아이콘/배지/TTL/긴급도/딥링크)을 구성한다.
     *
     * @param data 전송 데이터(딥링크 link 키 우선)
     * @return WebpushConfig
     */
    private WebpushConfig webpushConfigFor(Map<String, String> data) {
        String link = (data != null && data.get("link") != null && !data.get("link").isBlank())
                ? data.get("link")
                : webpushProps.getDefaultLink();

        return WebpushConfig.builder()
                .putHeader("TTL", String.valueOf(Math.max(0, webpushProps.getTtlSeconds())))
                .putHeader("Urgency", webpushProps.getUrgency())
                .setNotification(
                        WebpushNotification.builder()
                                .setIcon(webpushProps.getIcon())
                                .setBadge(webpushProps.getBadge())
                                .build()
                )
                .setFcmOptions(WebpushFcmOptions.withLink(link))
                .build();
    }

    /**
     * 토픽 전송 오버로드(HqTopic 사용).
     *
     * @param appType 앱 타입
     * @param topic   HQ 토픽 열거형
     * @param title   제목
     * @param body    본문
     * @param data    데이터
     * @return 메시지 ID
     */
    public String sendToTopic(AppType appType, HqTopic topic, String title, String body, Map<String, String> data) {
        return sendToTopic(appType, topic.value(), title, body, data);
    }

    /**
     * 구독 오버로드(HqTopic 사용).
     *
     * @param topic    HQ 토픽 열거형
     * @param memberId 회원 ID
     */
    public void subscribeToTopic(HqTopic topic, Long memberId) {
        subscribeToTopic(topic.value(), memberId);
    }

    /**
     * 구독 해제 오버로드(HqTopic 사용).
     *
     * @param topic    HQ 토픽 열거형
     * @param memberId 회원 ID
     */
    public void unsubscribeFromTopic(HqTopic topic, Long memberId) {
        unsubscribeFromTopic(topic.value(), memberId);
    }
}
