package com.boot.ict05_final_admin.domain.fcm.repository;

import com.boot.ict05_final_admin.domain.fcm.dto.FcmLogRowDto;
import com.boot.ict05_final_admin.domain.fcm.entity.QFcmSendLog;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * {@link FcmSendLogRepositoryCustom}의 QueryDSL 구현체.
 *
 * <p>최근 전송 로그를 DTO로 투영(projection)하여 반환한다.</p>
 *
 * @author 이경욱
 * @since 2025-11-10
 */
@Repository
@RequiredArgsConstructor
public class FcmSendLogRepositoryImpl implements FcmSendLogRepositoryCustom {

	private final JPAQueryFactory query;

	/**
	 * 최근 전송 로그를 최신순으로 조회하여 {@link FcmLogRowDto} 리스트로 반환한다.
	 *
	 * @param limit 조회 제한 수
	 * @return 최근 로그 DTO 목록
	 */
	@Override
	@Transactional(readOnly = true)
	public List<FcmLogRowDto> findRecent(int limit) {
		QFcmSendLog l = QFcmSendLog.fcmSendLog;
		return query
				.select(Projections.constructor(FcmLogRowDto.class,
						l.fcmSendLogId,
						l.topic,
						l.token,
						l.title,
						l.body,
						l.resultMessageId,
						l.resultError,
						l.sentAt
				))
				.from(l)
				.orderBy(l.sentAt.desc())
				.limit(limit)
				.setHint("org.hibernate.readOnly", true)
				.setHint("org.hibernate.flushMode", "COMMIT")
				.setHint("javax.persistence.query.timeout", 3000)
				.fetch();
	}
}
