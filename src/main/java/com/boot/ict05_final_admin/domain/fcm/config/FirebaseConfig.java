package com.boot.ict05_final_admin.domain.fcm.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.InputStream;

/**
 * Firebase SDK(FirebaseApp) 초기화를 담당하는 설정 클래스.
 *
 * <p>프로퍼티 {@code fcm.enabled=true} 일 때만 FirebaseApp 빈을 등록한다.
 * 서비스 계정 파일 경로는 {@code fcm.service-account} 프로퍼티로 지정한다.</p>
 *
 * @author 이경욱
 * @since 2025-11-10
 */
@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${fcm.service-account:}")
    private String serviceAccountPath;

    /**
     * FirebaseApp 빈을 생성하여 반환한다.
     *
     * @param resourceLoader 스프링의 {@link ResourceLoader}
     * @return 초기화된 {@link FirebaseApp} 인스턴스
     * @throws IllegalStateException 초기화 실패 또는 서비스 계정 파일 미발견 시 발생
     */
    @Bean
    @ConditionalOnProperty(name = "fcm.enabled", havingValue = "true", matchIfMissing = false)
    public FirebaseApp firebaseApp(ResourceLoader resourceLoader) {
        try {
            if (serviceAccountPath == null || serviceAccountPath.isBlank()) {
                throw new IllegalStateException(
                        "fcm.enabled=true 인데 fcm.service-account 경로가 비어있습니다. 예) classpath:firebase/service-account.json");
            }
            Resource resource = resourceLoader.getResource(serviceAccountPath);
            if (!resource.exists()) {
                throw new IllegalStateException("Firebase 서비스 계정 파일을 찾을 수 없습니다: " + serviceAccountPath);
            }
            try (InputStream is = resource.getInputStream()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(is))
                        .build();
                if (FirebaseApp.getApps().isEmpty()) {
                    log.info("[FCM] Initializing FirebaseApp with {}", serviceAccountPath);
                    return FirebaseApp.initializeApp(options);
                }
                return FirebaseApp.getInstance();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to init FirebaseApp: " + e.getMessage(), e);
        }
    }
}
