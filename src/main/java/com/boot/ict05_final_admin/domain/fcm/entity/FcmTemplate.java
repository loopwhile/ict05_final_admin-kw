package com.boot.ict05_final_admin.domain.fcm.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 알림 템플릿을 저장하는 엔티티.
 *
 * <p>템플릿 코드를 기준으로 제목/본문 템플릿을 관리하며, 템플릿 코드는 유니크 제약으로 중복을 허용하지 않는다.</p>
 *
 * @author 이경욱
 * @since 2025-11-10
 */
@Entity
@Table(name = "fcm_template")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FcmTemplate {

    /**
     * PK: 템플릿 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fcmTemplateId;

    /**
     * 템플릿 코드 (예: NOTICE_UPDATED, HQ_STOCK_LOW) - 유니크
     */
    @Column(nullable = false, unique = true, length = 64)
    private String templateCode;

    /**
     * 제목 템플릿 (변수 치환 포함 가능)
     */
    @Column(nullable = false, length = 200)
    private String titleTemplate;

    /**
     * 본문 템플릿 (변수 치환 포함 가능)
     */
    @Column(nullable = false, length = 1000)
    private String bodyTemplate;
}
