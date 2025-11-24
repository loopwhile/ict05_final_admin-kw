package com.boot.ict05_final_admin.domain.fcm.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * HQ 인벤토리 스캐너 설정을 위한 프로퍼티 바인딩 클래스.
 *
 * <p>스케줄러 on/off, cron 표현식, 임계/임박 탐지 상한 및 유통기한 임박 기준일 등을 외부 설정으로 주입받는다.</p>
 *
 * @author 이경욱
 * @since 2025-11-10
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "fcm.scanner")
public class FcmScannerProperties {

    /**
     * 스케줄러 활성화 여부.
     */
    private boolean enabled = false;

    /**
     * 크론 표현식(예: "0 0/30 * * * *").
     */
    private String cron = "0 0/30 * * * *";

    /**
     * 부족/임박 탐지 시 상한 행수(안전장치).
     */
    private int stockLowMax = 50;
    private int expireSoonMax = 50;

    /**
     * 유통기한 임박 기준일(일).
     */
    private int expireSoonDaysDefault = 3;
}
