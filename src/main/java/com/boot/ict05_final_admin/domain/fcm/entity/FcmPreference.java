package com.boot.ict05_final_admin.domain.fcm.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 사용자의 FCM 수신 선호(Preference)를 저장하는 엔티티.
 *
 * <p>앱/회원/점포/직원 단위로 알림 카테고리 수신 여부 및 임박 기준 일수를 관리한다.
 * 생성/갱신 시각을 관리하며, PrePersist에서 기본값을 보장한다.</p>
 *
 * @author 이경욱
 * @since 2025-11-10
 */
@Entity
@Table(name = "fcm_preference",
        indexes = {
                @Index(name="ix_pref_member", columnList = "member_id_fk"),
                @Index(name="ix_pref_store",  columnList = "store_id_fk"),
                @Index(name="ix_pref_staff",  columnList = "staff_id_fk")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FcmPreference {

    /**
     * PK: FCM Preference ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fcmPreferenceId;

    /**
     * 애플리케이션 타입 (HQ / STORE)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AppType appType;

    /**
     * 회원 ID (HQ 사용자)
     */
    @Column(name = "member_id_fk")
    private Long memberIdFk;

    /**
     * 점포 ID (가맹점)
     */
    @Column(name = "store_id_fk")
    private Long storeIdFk;

    /**
     * 직원 ID (가맹점 직원)
     */
    @Column(name = "staff_id_fk")
    private Long staffIdFk;

    /**
     * 공지(Notice) 카테고리 수신 여부 (기본 true)
     */
    @Column(nullable = false)
    private Boolean catNotice = true;

    /**
     * 재고 부족 카테고리 수신 여부 (기본 true)
     */
    @Column(nullable = false)
    private Boolean catStockLow = true;

    /**
     * 유통기한 임박 카테고리 수신 여부 (기본 true)
     */
    @Column(nullable = false)
    private Boolean catExpireSoon = true;

    /**
     * 유통기한 임박 기준일수 (기본 3일)
     */
    @Column(nullable = false)
    private Integer thresholdDays = 3;

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
    void onUpdate() { this.updatedAt = LocalDateTime.now(); }

    /**
     * 엔티티 생성 직전 콜백: 필수 필드의 기본값(createdAt, updatedAt, 카테고리 플래그, thresholdDays)을 보장한다.
     */
    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (appType == null) appType = AppType.HQ;
        if (catNotice == null) catNotice = true;
        if (catStockLow == null) catStockLow = true;
        if (catExpireSoon == null) catExpireSoon = true;
        if (thresholdDays == null) thresholdDays = 3;
    }
}
