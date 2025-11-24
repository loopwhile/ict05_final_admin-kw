package com.boot.ict05_final_admin.domain.fcm.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * FCM 전송 결과 로그를 저장하는 엔티티.
 *
 * <p>토픽/토큰, 전송 제목/본문, 전송시각, 결과 메시지 ID 또는 에러 등 전송 기록을 보관한다.</p>
 *
 * @author 이경욱
 * @since 2025-11-10
 */
@Entity
@Table(name = "fcm_send_log",
        indexes = {
                @Index(name="ix_log_store",  columnList = "store_id_fk"),
                @Index(name="ix_log_member", columnList = "member_id_fk"),
                @Index(name="ix_log_staff",  columnList = "staff_id_fk")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FcmSendLog {

    /**
     * PK: 전송 로그 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fcmSendLogId;

    /**
     * 애플리케이션 타입 (HQ / STORE)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AppType appType;

    /**
     * 전송된 토픽 (토픽 전송인 경우), 단일 토큰 전송이면 null
     */
    private String topic;

    /**
     * 전송 대상 토큰 (토큰 전송인 경우)
     */
    @Column(length = 512)
    private String token;

    /**
     * 전송된 알림 제목
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * 전송된 알림 본문
     */
    @Column(nullable = false, length = 1000)
    private String body;

    /**
     * 전송에 포함된 추가 데이터(JSON 문자열, 필요시 사용)
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String dataJson;

    /**
     * FCM으로부터 반환된 메시지 ID (성공 시)
     */
    private String resultMessageId;

    /**
     * 전송 실패 시 에러 메시지(스택/요약)
     */
    private String resultError;

    /**
     * 전송 시각 (비어 있으면 Persist 시점으로 설정)
     */
    @Column(nullable = false)
    private LocalDateTime sentAt = LocalDateTime.now();

    /**
     * 관련 점포/회원/직원 참조 (있다면)
     */
    @Column(name="store_id_fk")
    private Long storeIdFk;

    @Column(name="member_id_fk")
    private Long memberIdFk;

    @Column(name="staff_id_fk")
    private Long staffIdFk;

    /**
     * 엔티티 생성 직전 콜백: sentAt 기본값을 보장한다.
     */
    @PrePersist
    void onCreate() {
        if (sentAt == null) sentAt = LocalDateTime.now();
    }
}
