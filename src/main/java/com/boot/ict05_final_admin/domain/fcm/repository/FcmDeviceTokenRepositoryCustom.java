package com.boot.ict05_final_admin.domain.fcm.repository;

import com.boot.ict05_final_admin.domain.fcm.entity.AppType;

import java.util.List;

/**
 * FCM 디바이스 토큰에 대한 커스텀 리포지토리 인터페이스.
 *
 * <p>성능/운영 관점의 특화 조회를 여기에 정의한다 (QueryDSL 구현체가 제공됨).</p>
 *
 * @author 이경욱
 * @since 2025-11-10
 */
public interface FcmDeviceTokenRepositoryCustom {

    /**
     * HQ 멤버에 연결된 활성 토큰 문자열 목록을 조회한다.
     *
     * @param memberId HQ 회원 ID
     * @return 활성 토큰 문자열 목록
     */
    List<String> findActiveTokensForHqMember(Long memberId);

    /**
     * 지정된 AppType의 활성 토큰들을 제한(limit)수만큼 조회한다.
     *
     * @param appType 애플리케이션 타입 (HQ/STORE)
     * @param limit   조회 제한 수
     * @return 토큰 문자열 목록
     */
    List<String> findActiveTokensByAppType(AppType appType, int limit);
}
