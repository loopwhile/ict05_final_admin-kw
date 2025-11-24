package com.boot.ict05_final_admin.domain.fcm.repository;

import com.boot.ict05_final_admin.domain.fcm.entity.AppType;
import com.boot.ict05_final_admin.domain.fcm.entity.QFcmDeviceToken;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * {@link FcmDeviceTokenRepositoryCustom}의 QueryDSL 구현체.
 *
 * <p>성능을 고려해 readOnly 트랜잭션과 쿼리 힌트를 적용하고,
 * DB 정렬 비용이 불필요한 경우 ORDER BY NULL 패턴을 사용한다.</p>
 *
 * @author 이경욱
 * @since 2025-11-10
 */
@RequiredArgsConstructor
@Repository
public class FcmDeviceTokenRepositoryImpl implements FcmDeviceTokenRepositoryCustom {

    private final JPAQueryFactory query;

    /**
     * HQ 멤버에 연결된 활성 토큰 문자열을 조회한다.
     *
     * @param memberId HQ 회원 ID
     * @return 활성 토큰 문자열 목록
     */
    @Override
    @Transactional(readOnly = true)
    public List<String> findActiveTokensForHqMember(Long memberId) {
        QFcmDeviceToken t = QFcmDeviceToken.fcmDeviceToken;
        return query
                .select(t.token)
                .from(t)
                .where(
                        t.appType.eq(AppType.HQ),
                        t.isActive.isTrue(),
                        t.memberIdFk.eq(memberId)
                )
                .orderBy(Expressions.stringTemplate("NULL").asc())
                .setHint("org.hibernate.readOnly", true)
                .setHint("org.hibernate.flushMode", "COMMIT")
                .setHint("javax.persistence.query.timeout", 3000)
                .fetch();
    }

    /**
     * 지정된 AppType의 활성 토큰들을 limit 수만큼 조회한다.
     *
     * @param appType 애플리케이션 타입
     * @param limit   최대 조회 건수
     * @return 토큰 문자열 목록
     */
    @Override
    @Transactional(readOnly = true)
    public List<String> findActiveTokensByAppType(AppType appType, int limit) {
        QFcmDeviceToken t = QFcmDeviceToken.fcmDeviceToken;
        return query
                .select(t.token)
                .from(t)
                .where(t.appType.eq(appType), t.isActive.isTrue())
                .limit(limit)
                .orderBy(Expressions.stringTemplate("NULL").asc())
                .setHint("org.hibernate.readOnly", true)
                .setHint("org.hibernate.flushMode", "COMMIT")
                .setHint("javax.persistence.query.timeout", 3000)
                .fetch();
    }
}
