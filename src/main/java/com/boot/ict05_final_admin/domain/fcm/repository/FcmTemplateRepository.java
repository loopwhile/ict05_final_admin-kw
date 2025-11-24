package com.boot.ict05_final_admin.domain.fcm.repository;

import com.boot.ict05_final_admin.domain.fcm.entity.FcmTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * FCM 템플릿 엔티티에 대한 JPA 리포지토리 인터페이스.
 *
 * <p>템플릿 코드를 기준으로 템플릿을 조회하는 편의 메서드를 제공한다.</p>
 *
 * @author 이경욱
 * @since 2025-11-10
 */
public interface FcmTemplateRepository extends JpaRepository<FcmTemplate, Long> {

    /**
     * 템플릿 코드로 FcmTemplate을 조회한다.
     *
     * @param templateCode 템플릿 코드
     * @return 템플릿(존재 시), 없으면 Optional.empty()
     */
    Optional<FcmTemplate> findByTemplateCode(String templateCode);
}
