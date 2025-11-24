package com.boot.ict05_final_admin.domain.fcm.repository;

import com.boot.ict05_final_admin.domain.fcm.dto.FcmLogRowDto;

import java.util.List;

/**
 * FCM 전송 로그에 대한 커스텀 리포지토리 인터페이스.
 *
 * <p>로그 목록 조회와 같은 특화된 읽기 전용 쿼리를 정의한다.</p>
 *
 * @author 이경욱
 * @since 2025-11-10
 */
public interface FcmSendLogRepositoryCustom {

	/**
	 * 최근 전송 로그를 최신순으로 제한(limit)만큼 조회한다.
	 *
	 * @param limit 조회 제한 수
	 * @return 로그 행 DTO 리스트
	 */
	List<FcmLogRowDto> findRecent(int limit);
}
