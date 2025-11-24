package com.boot.ict05_final_admin.domain.fcm.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 디바이스 토큰 정보를 저장하는 엔티티.
 *
 * <p>앱/플랫폼별 디바이스 토큰과 토큰의 활성화 상태, 연결된 회원/점포/직원 참조 등을 보관한다.
 * 생성/갱신 시각(createdAt/updatedAt)과 마지막 접속 시각(lastSeenAt)을 유지한다.</p>
 *
 * @author 이경욱
 * @since 2025-11-10
 */
@Entity
@Table(name = "fcm_device_token",
        indexes = {
                @Index(name="ix_device_member", columnList = "member_id_fk"),
                @Index(name="ix_device_store",  columnList = "store_id_fk"),
                @Index(name="ix_device_staff",  columnList = "staff_id_fk")
        },
        uniqueConstraints = @UniqueConstraint(name="uq_fcm_token", columnNames = "token"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FcmDeviceToken {

    /**
     * PK: FCM 디바이스 토큰 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fcmDeviceTokenId;

    /**
     * 애플리케이션 타입 (HQ / STORE)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AppType appType;

    /**
     * 플랫폼 타입(ANDROID / IOS / WEB)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PlatformType platform;

    /**
     * FCM 토큰 문자열 (유일)
     */
    @Column(nullable = false, length = 512)
    private String token;

    /**
     * 디바이스 식별자(앱에서 제공하는 deviceId, 선택)
     */
    @Column(length = 128)
    private String deviceId;

    /**
     * 회원 ID (HQ 사용자 연결; HQ쪽에서는 서버가 세션 사용자로 오버라이드할 수 있음)
     */
    @Column(name = "member_id_fk")
    private Long memberIdFk;

    /**
     * 점포 ID (가맹점 연결; HQ에서는 비필수)
     */
    @Column(name = "store_id_fk")
    private Long storeIdFk;

    /**
     * 직원 ID (가맹점 직원 연결)
     */
    @Column(name = "staff_id_fk")
    private Long staffIdFk;

    /**
     * 토큰 활성화 여부 (기본 true)
     */
    @Column(nullable = false)
    private Boolean isActive = true;

    /**
     * 마지막으로 토큰 사용/연결이 확인된 시각 (optional)
     */
    private LocalDateTime lastSeenAt;

    /**
     * 생성 시각 (비어 있으면 Persist 시점으로 설정)
     */
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * 갱신 시각 (업데이트 시마다 갱신)
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * 엔티티 업데이트 직전 콜백: updatedAt을 현재 시각으로 갱신한다.
     */
    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 엔티티 생성 직전 콜백: createdAt/updatedAt과 isActive의 기본값을 보장한다.
     */
    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (isActive == null) isActive = true;
    }
}
