package com.boot.ict05_final_admin.domain.fcm.repository;

import com.boot.ict05_final_admin.domain.fcm.entity.AppType;
import com.boot.ict05_final_admin.domain.fcm.entity.FcmPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * FCM 수신 선호(FcmPreference) 엔티티에 대한 JPA 리포지토리.
 *
 * @author 이경욱
 * @since 2025-11-10
 */
public interface FcmPreferenceRepository extends JpaRepository<FcmPreference, Long> {

	/**
	 * 특정 앱 타입과 회원 ID에 대한 첫 번째 Preference를 조회한다.
	 *
	 * @param appType    애플리케이션 타입
	 * @param memberIdFk 회원 ID
	 * @return Optional로 래핑된 FcmPreference
	 */
	Optional<FcmPreference> findFirstByAppTypeAndMemberIdFk(AppType appType, Long memberIdFk);
}
