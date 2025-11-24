package com.boot.ict05_final_admin.domain.fcm.repository;

import com.boot.ict05_final_admin.domain.fcm.entity.FcmDeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * FCM 디바이스 토큰 엔티티에 대한 JPA 리포지토리 인터페이스.
 *
 * <p>스프링 데이터 JPA의 기본 CRUD 기능을 상속하며,
 * 토큰 기반 조회와 커스텀 리포지토리 메서드를 제공한다.</p>
 *
 * @author 이경욱
 * @since 2025-11-10
 */
public interface FcmDeviceTokenRepository
        extends JpaRepository<FcmDeviceToken, Long>, FcmDeviceTokenRepositoryCustom {

    /**
     * 토큰 문자열로 FcmDeviceToken 엔티티를 조회한다.
     *
     * @param token FCM 토큰 문자열
     * @return 토큰에 해당하는 엔티티(없으면 Optional.empty())
     */
    Optional<FcmDeviceToken> findByToken(String token);
}
